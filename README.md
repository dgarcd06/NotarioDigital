
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
Notario Digital está desarrollado en Java, con el JDK-21. Será necesaria su instalación para la ejecución de la aplicación. Se puede encontrar a través del siguiente enlace: https://www.oracle.com/es/java/technologies/downloads/#java21
Una vez instalado, tan sólo será necesaria la ejecución del archivo NotarioDigital.jar, ejecutable de la aplicación que incluye las dependencias necesarias para el uso.



### Dependencias
...

## Autor/es
---
El autor de la herramienta es David García Diez. El desarrollo forma parte del uso de diferentes frameworks adicionales que apoyan la implementación.

## Información adicional
---
...

## Licencia 
---

La licencia es de uso libre...
