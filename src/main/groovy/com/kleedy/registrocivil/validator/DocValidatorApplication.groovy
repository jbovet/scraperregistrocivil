package com.kleedy.registrocivil.validator

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.web.SpringBootServletInitializer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static org.jsoup.Jsoup.parse

/**
 * Created by josebovet on 3/24/16.
 */
@SpringBootApplication
@RestController
class DocValidatorApplication extends SpringBootServletInitializer {

    static void main(String[] args) {
        SpringApplication.run DocValidatorApplication, args
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DocValidatorApplication.class)
    }

    def http = new HTTPBuilder('https://portal.sidiv.registrocivil.cl/usuarios-portal/pages/DocumentRequestStatus.xhtml');

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity index() {
        return new ResponseEntity(true, HttpStatus.OK)
    }


        @RequestMapping(value = "/validate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity check(@Valid @RequestBody DocumentRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            //TODO validar tipos de documentos.
            return new ResponseEntity(["valid": false, "status": "DATOS INVALIDOS EN LA CONSULTA. * TODOS LOS DATOS SON REQUERIDOS *"], HttpStatus.OK)
        }
        //magic
        return new ResponseEntity(query(request), HttpStatus.OK)
    }

    def query(request) {
        def viewState, html
        http.ignoreSSLIssues() //ignorar ssl

        try {
            http.request(Method.GET, TEXT) { req ->
                response.success = { resp, reader ->
                    assert resp.status == 200
                    def doc = parse(reader.text)
                    viewState = doc.select("input[id=javax.faces.ViewState]").attr("value")
                }
                response.failure = { resp -> println resp.statusLine }
            }

            //build payload
            def payload = ["form"          : "form", "form:run": request.run, "form:selectDocType": request.docType,
                           "form:docNumber": request.docNumber, "form:buttonHidden": "", "javax.faces.ViewState": viewState]

            http.request(Method.POST) {
                requestContentType = URLENC
                body = payload
                response.success = { resp, reader ->
                    assert resp.statusLine.statusCode == 200
                    html = reader.text()
                }
                response.failure = { resp -> println resp.statusLine }
            }
        } catch (Exception e) {
            return ["valid": false, "status": "OCURRIO UN ERROR AL VALIDAR DOCUMENTO, NO SE PUDO ACCEDER AL SERVICIO DEL REGISTRO CIVIL."]
        }
        return buildMessage(html)
    }

    def buildMessage(html) {
        boolean estado = false
        String msg = "NO SE PUDO VALIDAR DOCUMENTO."
        if (html.contains("ESTADOVigente")) {
            estado = true
            msg = "VIGENTE"
        } else if (html.contains("ESTADONo Vigente")) {
            msg = "NO VIGENTE"
        } else if (html.contains("ErrorLa información ingresada no corresponde en nuestros registros")) {
            msg = "LA INFORMACIÓN INGRESADA NO CORRESPONDE A LOS REGISTROS, DOCUMENTO INVALIDO."
        } else if (html.contains("los campos destacados son incorrectos o están incompletos")) {
            msg = "TIPO DE DOCUMENTO NO VALIDO."
        }
        return ["valid": estado, "status": msg]
    }
}
