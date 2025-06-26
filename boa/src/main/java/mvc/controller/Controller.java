package mvc.controller;

import trunk.http11.request.HttpRequest;
import trunk.http11.response.HttpResponse;

public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
