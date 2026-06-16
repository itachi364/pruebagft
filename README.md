# Renombramiento Inteligente de Archivos en S3

## Resumen

Solucion Fullstack Cloud para normalizar nombres de archivos recibidos en Amazon S3. El sistema lista archivos desde S3 o desde una simulacion local, aplica reglas configurables de mapeo, normaliza fechas embebidas, almacena resultados de procesamiento y expone una interfaz operativa para revisar resultados, administrar reglas y reprocesar archivos.

## Funcionalidades

- Listado de archivos desde S3 o LocalStack.
- Transformacion de nombres no estandarizados a layouts esperados por el banco.
- Motor de reglas configurable y versionable.
- Estados de procesamiento: `Transformado`, `Error` y `No mapeado`.
- Registro de archivo origen, nombre destino, regla aplicada y detalle de error o no mapeo.
- Panel operativo para resumen de resultados.
- Administracion de reglas.
- Reprocesamiento de archivos despues de cambios en reglas.
- Ejecucion local con Docker Compose y LocalStack.
- Despliegue objetivo en AWS con Lambda, API Gateway, S3, DynamoDB, CloudFront y AWS SAM.

## Arquitectura

La arquitectura objetivo es hexagonal modular.

- El motor de reglas y los casos de uso no dependen de AWS ni de Spring.
- S3 y DynamoDB se integran mediante adaptadores de salida.
- API Gateway, Lambda y Spring Boot funcionan como mecanismos de entrada.
- Angular provee la interfaz de operacion.
- AWS SAM describe la infraestructura cloud.

Diagrama de arquitectura:

```text
docs/diagrams/architecture.mmd
```

## Stack Tecnologico

| Area | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 3 |
| Runtime cloud | AWS Lambda Java 17 |
| API | API Gateway HTTP API |
| Frontend | Angular |
| Almacenamiento | Amazon S3 |
| Base de datos | DynamoDB |
| Cola opcional | SQS |
| Frontend hosting | S3 + CloudFront |
| Infraestructura | AWS SAM |
| Local cloud | LocalStack |
| Orquestacion local | Docker Compose |
| Pruebas backend | JUnit 5, Mockito, AssertJ, JaCoCo |
| Calidad | SonarQube / Sonar scanner |

## Estructura del Proyecto

Estructura esperada de la aplicacion:

```text
.
|-- backend/
|-- frontend/
|-- infrastructure/
|   |-- localstack/
|   `-- sam/
|-- data/
|   `-- samples/
|-- docs/
|   `-- diagrams/
|-- docker-compose.yml
|-- .env.example
|-- sonar-project.properties
`-- README.md
```

## Requisitos

Herramientas esperadas para desarrollo, pruebas y ejecucion local:

- Java 17
- Maven
- Node.js LTS
- Angular CLI
- Docker y Docker Compose
- LocalStack
- AWS SAM CLI
- Sonar scanner o integracion Maven Sonar

## Variables de Entorno

No se deben guardar secretos reales en archivos versionados.

| Variable | Descripcion | Ejemplo |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Access key dummy para LocalStack | `test` |
| `AWS_SECRET_ACCESS_KEY` | Secret key dummy para LocalStack | `test` |
| `AWS_REGION` | Region AWS | `us-east-1` |
| `S3_BUCKET_NAME` | Bucket de entrada | `file-renaming-input-local` |
| `RULES_TABLE_NAME` | Tabla de reglas | `file-renaming-rules-local` |
| `BATCHES_TABLE_NAME` | Tabla de lotes | `file-renaming-batches-local` |
| `RESULTS_TABLE_NAME` | Tabla de resultados | `file-renaming-results-local` |
| `SONAR_HOST_URL` | URL del servidor SonarQube | `http://localhost:9000` |
| `SONAR_TOKEN` | Token local de analisis | No guardar tokens reales |
| `SONAR_PROJECT_KEY` | Project key de SonarQube | `SONAR_PROJECT_KEY` |

## Ejecucion Local

La ejecucion local esta disenada para usar Docker Compose y LocalStack. LocalStack simula los servicios AWS necesarios para desarrollo:

- S3 para archivos de entrada.
- DynamoDB para reglas, lotes y resultados.
- SQS si se habilita procesamiento asincrono.

Backend local:

```bash
cd backend
mvn spring-boot:run
```

Frontend local:

```bash
cd frontend
npm install
npm start
```

La URL del frontend local es:

```text
http://localhost:4200
```

## Ejecucion con Docker Compose

El entorno local debe levantar:

- Backend Spring Boot.
- Frontend Angular.
- LocalStack.
- Recursos locales de S3 y DynamoDB.
- Datos simulados para pruebas.

Comando objetivo:

```bash
docker compose up --build
```

URLs locales:

```text
Frontend: http://localhost:4200
Backend health: http://localhost:8080/api/health
LocalStack: http://localhost:4566
```

## Ejecucion en AWS

La solucion esta orientada a los siguientes recursos AWS:

- Lambda para el backend Java.
- API Gateway HTTP API para endpoints REST.
- S3 para entrada de archivos.
- DynamoDB para persistencia.
- S3 + CloudFront para publicar el frontend Angular.
- SQS opcional para procesamiento asincrono.
- CloudWatch Logs para observabilidad.

Comandos objetivo con AWS SAM:

```bash
sam validate
sam build
sam deploy --guided
```

Antes de desplegar se deben configurar parametros por ambiente, roles IAM de menor privilegio y variables seguras fuera del repositorio.

Despliegue automatizado desde PowerShell:

```powershell
.\scripts\deploy-aws.ps1 `
  -AccountId "<aws-account-id>" `
  -Region "<aws-region>" `
  -StageName "dev" `
  -StackName "s3-renaming-dev"
```

El script ejecuta `mvn clean verify`, construye el frontend, valida SAM, despliega infraestructura, publica el frontend en S3, invalida CloudFront y carga datos simulados.

Los nombres de buckets deben ser globalmente unicos. La convencion recomendada es:

```text
s3-renaming-input-<aws-account-id>-<aws-region>-<stage>
s3-renaming-frontend-<aws-account-id>-<aws-region>-<stage>
```

Validacion de outputs:

```bash
aws cloudformation describe-stacks \
  --stack-name s3-renaming-dev \
  --region <aws-region> \
  --query "Stacks[0].Outputs"
```

Validacion funcional posterior:

```bash
curl "<ApiUrl-del-output>/api/health"
curl "<ApiUrl-del-output>/api/files"
```

Para eliminar el stack:

```bash
sam delete --stack-name s3-renaming-dev --region <aws-region>
```

## Ambientes

### Local

- Docker Compose.
- LocalStack.
- Credenciales dummy.
- Datos simulados.

### Desarrollo AWS

- Recursos creados con AWS SAM.
- Variables separadas por ambiente.
- Tablas y buckets no productivos.

### Produccion

- Cuentas o stacks separados.
- IAM de menor privilegio.
- Logs y monitoreo en CloudWatch.
- Secretos gestionados fuera del repositorio.
- Autenticacion y autorizacion a definir antes de exponer datos sensibles.

## Base de Datos

DynamoDB es el motor de persistencia seleccionado.

Tablas objetivo:

- `rules`: reglas, versiones y estado activo/inactivo.
- `processing_batches`: lotes y resumen de procesamiento.
- `processing_results`: resultado por archivo procesado.

El diseno esta basado en patrones de acceso para evitar modelado relacional innecesario.

## Motor de Reglas

El motor de reglas debe:

- Evaluar patrones configurables.
- Ignorar extensiones cuando aplique.
- Detectar fechas embebidas.
- Normalizar fechas a `YYYYMMDD`.
- Resolver subtipos.
- Generar nombres destino.
- Marcar archivos sin regla como `No mapeado`.
- Reportar errores controlados sin exponer detalles internos.

Mapeos base:

| Patron origen | Layout destino |
|---|---|
| `PHO_CD_DES_*` | `01_Estructura CDT Desmaterializado` |
| `PHO_SV_*` | `03_Estructura Cuenta Ahorros` |
| `PHO_CK_*` | `04_Estructura Cuenta Corriente` |
| `PHO_ML_*` | Layout segun subtipo configurado |
| `garantias_*` | Layout `14_*` segun subtipo |
| `activos_*` | Layout `37_Leasing_*` segun subtipo |

## Pruebas

La logica de negocio nueva o modificada debe apuntar a 100% de branch coverage.

Comandos backend objetivo:

```bash
cd backend
mvn test
mvn verify
```

Comandos frontend:

```bash
cd frontend
npm test
npm run build
```

Reporte de cobertura backend:

```text
backend/target/site/jacoco/index.html
```

Reporte de cobertura frontend:

```text
frontend/coverage/s3-renaming-frontend/index.html
```

## Calidad SonarQube

Configuracion objetivo:

- `sonar-project.properties`.
- JaCoCo para cobertura backend.
- Reporte de cobertura compatible con SonarQube.
- Variables `SONAR_HOST_URL`, `SONAR_TOKEN` y `SONAR_PROJECT_KEY` por entorno local/CI.

Comando objetivo:

```bash
cd backend
mvn verify
cd ..
sonar-scanner
```

No se debe guardar un token real de SonarQube en el repositorio.

## Seguridad

- No guardar credenciales reales de AWS.
- Usar credenciales dummy para LocalStack.
- Validar entradas externas de la API.
- No registrar secretos ni datos sensibles.
- Usar variables de entorno para configuracion runtime.
- Aplicar IAM de menor privilegio en AWS SAM.

## Observabilidad

- Logs estructurados.
- `correlationId` por request.
- `batchId` en operaciones de procesamiento.
- CloudWatch Logs en AWS.
- Errores publicos sin detalles internos sensibles.

## Solucion de Problemas

| Problema | Solucion |
|---|---|
| Faltan recursos en LocalStack | Reejecutar el script de inicializacion local. |
| Falta token Sonar | Definir `SONAR_TOKEN` localmente y no guardarlo en el repositorio. |
| Docker Compose no inicia | Revisar Docker Desktop/Engine y los logs de compose. |
| No aparecen archivos de entrada | Verificar el bucket configurado y los datos simulados. |

## Licencia

Open Source.
