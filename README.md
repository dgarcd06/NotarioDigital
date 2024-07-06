
*Notario Digital es una herramienta de Firma Digital y Verificación desarrollada en Java y basada en el algoritmo post-cuántico estandarizado Dilithium, basado en retículos.*

<h1 align="center">Notario Digital</h1>
<p align="center"><img src="https://github.com/dgarcd06/NotarioDigital/blob/main/recursos/icono_jframe.png"/></p> 
<p align="center"> Logo e icono de Notario Digital</p>
## Tabla de contenidos:
---

- [Descripción y contexto](#descripción-y-contexto)
- [Guía de usuario](#guía-de-usuario)
- [Guía de instalación](#guía-de-instalación)
- [Autor/es](#autores)
- [Información adicional](#información-adicional)
- [Licencia](#licencia)


## Descripción y contexto
---
Notario Digital permite la firma de documentos PDF mediante el uso de *Dilithium*, de forma que se pueden usar sus tres niveles de seguridad, estandarizados por el NIST en https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.204.ipd.pdf

Además, la aplicación detecta firmas digitales que utilicen algoritmos clásicos, y permiten la verificación recuperando sus datos, de manera que el usuario pueda verlos.

La página web oficial de Dilithium con el código de referencia del NIST es: https://pq-crystals.org/dilithium/
## Guía de usuario
---
La aplicación mostrará un frame principal en el que se permitirá cargar un archivo PDF. Se podrá navegar a través de este. Al seleccionar la Firma Visual, se podrá seleccionar un área para incluir una firma Digital
en las coordenadas elegidas. 
Al seleccionar Firma No Visual, se agregará una firma digital no visual al documento. 
En la operación de firmado, NO se altera el PDF original, sino que se crea uno nuevo en la ubicación del original, cuyo nombre incluirá el sufijo "_firmado". Al terminar una firma, se carga el nuevo PDF generado.
Al existir firmas en el documento cargado, se podrá abrir la opción de Verificación, mostrando la Información
relativa a las firmas que se encuentren. En la verificación se encontrarán datos de la firma, de la clave pública y del certificado digital. 
 	
## Guía de instalación
---
Notario Digital está desarrollado en Java, con el JDK-21. Será necesaria su instalación para la ejecución de la aplicación. Se puede encontrar a través del siguiente enlace: 
ORACLE -> https://www.oracle.com/es/java/technologies/downloads/#java21
OPENJDK -> https://openjdk.org/projects/jdk/21/
Una vez instalado, se debe descargar el proyecto (bien clonando el repositorio o descargándolo en zip). Al ejecutar el archivo .jar se iniciará la aplicación.
¡IMPORTANTE! No se debe mover el archivo ejecutable de su carpeta de origen; la aplicación dejaría de funcionar correctamente. Este caso se puede solucionar creando un Acceso Directo.


### Dependencias
JAVA JDK-21 para la ejecución de la aplicación. El código de Dilithium encontrado en la carpeta "recursos" está desarrollado en C, por lo que necesitará un compilador como GCC para Linux. Se puede utilizar MINGW para su uso en Windows, pero el archivo Makefile también está diseñado para usarse en Linux

## Autor/es
---
El autor de la herramienta es David García Diez. El desarrollo forma parte del uso de diferentes frameworks adicionales que apoyan la implementación.

## Información adicional
---
Desde este proyecto, animo a la comunidad al desarrollo de una criptografía Post-Cuántica.

## Licencia 
---

La licencia es de uso libre. Las librerías externas utilizan Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0) y la licencia MIT (https://opensource.org/license/MIT), la cual permite el uso y modificación libres del código.
