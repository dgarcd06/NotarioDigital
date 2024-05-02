#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "rng.h"
#include "sign.h"

#define	MAX_MARKER_LEN      50

#define KAT_SUCCESS          0
#define KAT_FILE_OPEN_ERROR -1
#define KAT_DATA_ERROR      -3
#define KAT_CRYPTO_FAILURE  -4

//ReadHex ha sido modificado para leer desde el comienzo del archivo
int	FindMarker(FILE *infile, const char *marker);
int	ReadHex(FILE *infile, unsigned char *a, int Length, char *str);
void	fprintBstr(FILE *fp, char *s, unsigned char *a, unsigned long long l);

/**
 * ***FIRMA DIG***
 * En primer lugar se crea la clave publica y privada y se almacenan en archivos separados
 * Estos se generan a partir de una semilla? 
 * Despues se recibe el mensaje y se firma(se encripta) y se almacena en un archivo
 * 
 * ***VERIFICACION***
 * Una vez estén creados y almacenados comienza el código de verificacion.
 * Se recoge la información de los distintos archivos creados (MENOS LA CLAVE PRIVADA OBVIO).
 * Se necesita la clave publica y el mensaje encriptado.
 * Despues se descifrará y se verificará, imprimiendo si es correcto o no en un printf
*/

int
main()
{
    char                fn_rsp[32] = "PQCsignKAT.rsp";
    FILE                *fp_rsp;
    uint8_t             seed[48];
    uint8_t             *msg;
    uint8_t             entropy_input[48];
    uint8_t             *m, *sm, *m1;
    size_t              mlen, smlen, mlen1;
    int                 count;
    int                 done;
    uint8_t             pk[CRYPTO_PUBLICKEYBYTES], sk[CRYPTO_SECRETKEYBYTES];
    int                 ret_val;

     /**ABRIMOS EL FICHERO CON LA INFORMACION**/
	 if ( (fp_rsp = fopen(fn_rsp, "r")) == NULL ) {
        printf("Couldn't open <%s> for read\n", fn_rsp);
        return KAT_FILE_OPEN_ERROR;
    }
	if ( FindMarker(fp_rsp, "mlen = ") )
        fscanf(fp_rsp, "%lu", &mlen);
    else {
        printf("ERROR: unable to read 'mlen' from <%s>\n", fn_rsp);
        return KAT_DATA_ERROR;
    }
	//Almacenamos memoria para el mensaje una vez obtenido su tamaño
    m = (uint8_t *)calloc(mlen, sizeof(uint8_t));
    m1 = (uint8_t *)calloc(mlen+CRYPTO_BYTES, sizeof(uint8_t));
    sm = (uint8_t *)calloc(mlen+CRYPTO_BYTES, sizeof(uint8_t));	

	if ( !ReadHex(fp_rsp, m, (int)mlen, "msg = ") ) {
        printf("ERROR: unable to read 'msg' from <%s>\n", fn_rsp);
        return KAT_DATA_ERROR;
    }

	//Recogemos la clave publica
	if ( !ReadHex(fp_rsp, pk, CRYPTO_PUBLICKEYBYTES, "pk = ") ) {
        printf("ERROR: unable to read 'pk' from <%s>\n", fn_rsp);
        return KAT_DATA_ERROR;
    }

	//Recogemos la firma y su longitud
	if ( FindMarker(fp_rsp, "smlen = ") )
        fscanf(fp_rsp, "%lu", &smlen);
    else {
        printf("ERROR: unable to read 'mlen' from <%s>\n", fn_rsp);
        return KAT_DATA_ERROR;
    }

	if ( !ReadHex(fp_rsp, sm, (int)smlen, "sm = ") ) {
        printf("ERROR: unable to read 'sm' from <%s>\n", fn_rsp);
        return KAT_DATA_ERROR;
    }
	//Una vez recogidos los datos, verificamos la firma
	if ( (ret_val = crypto_sign_open(m1, &mlen1, sm, smlen, pk)) != 0) {
        printf("No se ha podido verificar la firma: returned <%d>\n", ret_val);
        return KAT_CRYPTO_FAILURE;
    }else{
		printf("La firma ha sido verificada con éxito.\n");
	}
	/*FUNCIONA
	char mensaje_recuperado[33 + 1];  // +1 para el carácter nulo al final
    strncpy(mensaje_recuperado, (char *)m, 33);
    mensaje_recuperado[33] = '\0';  // Agregar el carácter nulo al final

    // Mostrar el mensaje recuperado
    printf("Mensaje recuperado: %s\n", mensaje_recuperado);
	*/
    return KAT_SUCCESS;
}

//
// ALLOW TO READ HEXADECIMAL ENTRY (KEYS, DATA, TEXT, etc.)
//
int
FindMarker(FILE *infile, const char *marker)
{
	char	line[MAX_MARKER_LEN];
	int	i, len;
	int	curr_line;

	len = (int)strlen(marker);
	if ( len > MAX_MARKER_LEN-1 )
	    len = MAX_MARKER_LEN-1;

	for ( i=0; i<len; i++ )
	  {
	    curr_line = fgetc(infile);
	    line[i] = curr_line;
	    if (curr_line == EOF )
	      return 0;
	  }
	line[len] = '\0';

	while ( 1 ) {
		if ( !strncmp(line, marker, len) )
			return 1;

		for ( i=0; i<len-1; i++ )
			line[i] = line[i+1];
		curr_line = fgetc(infile);
		line[len-1] = curr_line;
		if (curr_line == EOF )
			return 0;
		line[len] = '\0';
	}

	// shouldn't get here
	return 0;
}

//
// ALLOW TO READ HEXADECIMAL ENTRY (KEYS, DATA, TEXT, etc.)
//
int
ReadHex(FILE *infile, unsigned char *a, int Length, char *str)
{
	int		i, ch, started;
	unsigned char	ich;

	if ( Length == 0 ) {
		a[0] = 0x00;
		return 1;
	}
	memset(a, 0x00, Length);
	started = 0;
	if ( FindMarker(infile, str) )
		while ( (ch = fgetc(infile)) != EOF ) {
			if ( !isxdigit(ch) ) {
				if ( !started ) {
					if ( ch == '\n' )
						break;
					else
						continue;
				}
				else
					break;
			}
			started = 1;
			if ( (ch >= '0') && (ch <= '9') )
				ich = ch - '0';
			else if ( (ch >= 'A') && (ch <= 'F') )
				ich = ch - 'A' + 10;
			else if ( (ch >= 'a') && (ch <= 'f') )
				ich = ch - 'a' + 10;
			else // shouldn't ever get here
				ich = 0;

			for ( i=0; i<Length-1; i++ )
				a[i] = (a[i] << 4) | (a[i+1] >> 4);
			a[Length-1] = (a[Length-1] << 4) | ich;
		}
	else
		return 0;

	return 1;
}

void
fprintBstr(FILE *fp, char *s, unsigned char *a, unsigned long long l)
{
	unsigned long long  i;

	fprintf(fp, "%s", s);

	for ( i=0; i<l; i++ )
		fprintf(fp, "%02X", a[i]);

	if ( l == 0 )
		fprintf(fp, "00");

	fprintf(fp, "\n");
}


