package webserver.controller;

import webserver.http11.request.HttpRequest;
import webserver.http11.response.HttpResponse;

public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
