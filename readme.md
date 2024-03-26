# ToyS3

Este projeto é uma tentativa de replicar um S3 (Simple Storage Service) com bastante foco em arquivos
de dados estruturados como CSV, Parquet, ORC, Avro e afins. Ele terá algumas features a mais comparado com outros
serviços mais conhecidos como:

## Metadados dos arquivos.

- Tamanho
- Valores distintos
- Schema (colunas e tipos)
- Quantidade de linhas



# Code Patterns

## Abbreviations

Navegando no código ou nos arquivos de metadata pode ser que você encontre algumas abreviações/siglas, elas
variam de 2 a 3 letras que podem deixar você confuso, e se eventualmente você for fazer qualquer alteração
nós recomendamos que você utilize essas sigles caso elas se encaixem.

**pvt**: private (private field read only for the app)

**dt**: date (date field. Ex: YYYY-MM-DD)

**tms**: timestamp (timestamp field: Ex: YYYY-MM-DD HH:MM:SSS)

**dlt**: delimiter (delimiter character)
