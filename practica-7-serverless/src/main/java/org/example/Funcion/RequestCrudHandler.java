package org.example.Funcion;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.*;
import com.google.gson.Gson;
import org.example.Entidad.RequestData;
import org.example.Servicio.RequestDynamoOperations;

import java.util.HashMap;

public class RequestCrudHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final RequestDynamoOperations dbService = new RequestDynamoOperations();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        context.getLogger().log("[INFO] Input request: " + gson.toJson(request));
        String method = request.getHttpMethod();
        context.getLogger().log("[INFO] HTTP Method: " + method);

        String output = "";
        switch (method) {
            case "GET":
                RequestDynamoOperations.ListResponse listResp = dbService.listRequests(null, context);
                output = gson.toJson(listResp);
                break;

            case "POST":
            case "PUT":
                RequestData req = gson.fromJson(request.getBody(), RequestData.class);
                if(req.getUniqueId() != null && req.getUniqueId().isEmpty()){
                    req.setUniqueId(null);
                }
                RequestDynamoOperations.SingleResponse singleResp = dbService.saveRequest(req, context);
                output = gson.toJson(singleResp);
                break;

            case "DELETE":
                RequestData toDelete = gson.fromJson(request.getBody(), RequestData.class);
                dbService.removeRequest(toDelete, context);
                output = gson.toJson(toDelete);
                break;
        }

        // Cabeceras de respuesta
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Construimos la respuesta
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(headers);
        response.setBody(output);

        return response;
    }
}
