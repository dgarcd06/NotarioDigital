//
//  PQCgenKAT_sign.c
//
//  Created by Bassham, Lawrence E (Fed) on 8/29/17.
//  Copyright © 2017 Bassham, Lawrence E (Fed). All rights reserved.
//
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

int	FindMarker(FILE *infile, const char *marker);
int	ReadHex(FILE *infile, unsigned char *a, int Length, char *str);
void	fprintBstr(FILE *fp, char *s, unsigned char *a, unsigned long long l);

int
main()
{
    char                fn_rsp[32], mensaje[1000];
    FILE                *fp_rsp;
    uint8_t             seed[48];
    uint8_t             *msg;
    uint8_t             entropy_input[48];
    uint8_t             *sm, *m1;
    size_t              mlen, smlen, mlen1;
    int                 count;
    uint8_t             pk[CRYPTO_PUBLICKEYBYTES], sk[CRYPTO_SECRETKEYBYTES];
    int                 ret_val;


    //Se pide el mensaje a firmar y se convierte en Bytes
    printf("Introduce un mensaje para firmar: ");
    scanf("%s",mensaje);
    //Los mensajes en Bytes de Dilithium son de mlen = 33 * l siendo l => int
    int l;
    for(l=1;l<10;l++){
        if(strlen(mensaje) < 33 * l)
            break;
    }
    mlen = 33 * l;
    msg = (uint8_t *)calloc(mlen+CRYPTO_BYTES, sizeof(uint8_t));
    memset(msg,0,mlen); //Se inicializa el msg en Bytes a 0

    strncpy((char *)msg, mensaje, mlen);
    sprintf(fn_rsp, "PQCsignKAT.rsp");
    if ( (fp_rsp = fopen(fn_rsp, "w")) == NULL ) {
        printf("Couldn't open <%s> for write\n", fn_rsp);
        return KAT_FILE_OPEN_ERROR;
    }
        fprintf(fp_rsp, "mlen = %lu\n", mlen);

        m1 = (uint8_t *)calloc(mlen+CRYPTO_BYTES, sizeof(uint8_t));
        sm = (uint8_t *)calloc(mlen+CRYPTO_BYTES, sizeof(uint8_t));

        fprintBstr(fp_rsp, "msg = ", msg, mlen);

        // Generate the public/private keypair
        if ( (ret_val = crypto_sign_keypair(pk, sk)) != 0) {
            printf("crypto_sign_keypair returned <%d>\n", ret_val);
            return KAT_CRYPTO_FAILURE;
        }
        fprintBstr(fp_rsp, "pk = ", pk, CRYPTO_PUBLICKEYBYTES);
        fprintBstr(fp_rsp, "sk = ", sk, CRYPTO_SECRETKEYBYTES);

        if ( (ret_val = crypto_sign(sm, &smlen, msg, mlen, sk)) != 0) {
            printf("crypto_sign returned <%d>\n", ret_val);
            return KAT_CRYPTO_FAILURE;
        }
        fprintf(fp_rsp, "smlen = %lu\n", smlen);
        fprintBstr(fp_rsp, "sm = ", sm, smlen);
        fprintf(fp_rsp, "\n");

        if ( (ret_val = crypto_sign_open(m1, &mlen1, sm, smlen, pk)) != 0) {
            printf("crypto_sign_open returned <%d>\n", ret_val);
            return KAT_CRYPTO_FAILURE;
        }

        if ( mlen != mlen1 ) {
            printf("crypto_sign_open returned bad 'mlen': Got <%lu>, expected <%lu>\n", mlen1, mlen);
            return KAT_CRYPTO_FAILURE;
        }

        if ( memcmp(msg, m1, mlen) ) {
            printf("crypto_sign_open returned bad 'm' value\n");
            return KAT_CRYPTO_FAILURE;
        }


    fclose(fp_rsp);

    free(m1);
    free(sm);

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


