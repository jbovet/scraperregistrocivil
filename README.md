# Validador Documento Registro Civil
Base url: http://validadorregistrocivil-jbovet.rhcloud.com


Method POST                     PARAMS(JSON)
/validate                       run         * required
                                docNumber
                                docType     * required  (Types)
                                                        CEDULA
                                                        CEDULA_EXT
                                                        PASAPORTE_PG
                                                        PASAPORTE_DIPLOMATICO
                                                        PASAPORTE_OFICIAL


Response:                       true:  Valid
Response:                       false: Invalid document or document could not be validated.


Example:
Request:
```sh
curl --data '{"run":"11111111-1", "docType":"CEDULA" , "docNumber":"123"}' -v -X POST -H 'Content-Type:application/json' http://validadorregistrocivil-jbovet.rhcloud.com/validate
```
Response:
```sh
{"valid":false,"status":"LA INFORMACIÓN INGRESADA NO CORRESPONDE A LOS REGISTROS, DOCUMENTO INVALIDO."}%
```