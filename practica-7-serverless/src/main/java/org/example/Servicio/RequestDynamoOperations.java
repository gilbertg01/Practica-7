package org.example.Servicio;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import org.example.Entidad.RequestData;

import java.util.*;

public class RequestDynamoOperations {

    public SingleResponse saveRequest(RequestData req, Context context){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        if(req.getStudentId().isEmpty() || req.getFullName().isEmpty() ||
                req.getEmailAddress().isEmpty() || req.getLabName().isEmpty() ||
                req.getReserveDate().isEmpty() || req.getReserveHour().isEmpty()){
            throw new RuntimeException("Los campos enviados no pueden estar vacíos.");
        }

        Map<String, AttributeValue> exprValues = new HashMap<>();
        exprValues.put(":resDate", new AttributeValue().withS(req.getReserveDate()));
        exprValues.put(":resHour", new AttributeValue().withS(req.getReserveHour()));

        DynamoDBScanExpression scanExp = new DynamoDBScanExpression()
                .withFilterExpression("fecha = :resDate AND hora = :resHour")
                .withExpressionAttributeValues(exprValues);

        List<RequestData> results = mapper.scan(RequestData.class, scanExp);
        if(results.size() < 7){
            try {
                mapper.save(req);
            } catch (Exception e) {
                return new SingleResponse(true, e.getMessage(), null);
            }
            return new SingleResponse(false, "", req);
        }
        return new SingleResponse(true, "Se alcanzó el límite de 7 reservas en ese horario", null);
    }

    public ListResponse listRequests(FilterObject filter, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        List<RequestData> data = mapper.scan(RequestData.class, new DynamoDBScanExpression());
        return new ListResponse(false, "", data);
    }

    public SingleResponse removeRequest(RequestData request, Context context){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        mapper.delete(request);
        return new SingleResponse(false, null, request);
    }

    public static class ListResponse {
        boolean error;
        String errorMsg;
        List<RequestData> requests;

        public ListResponse() {
        }
        public ListResponse(boolean error, String errorMsg, List<RequestData> requests) {
            this.error = error;
            this.errorMsg = errorMsg;
            this.requests = requests;
        }

        public boolean isError() { return error; }
        public String getErrorMsg() { return errorMsg; }
        public List<RequestData> getRequests() { return requests; }
    }

    public static class SingleResponse {
        boolean error;
        String errorMsg;
        RequestData requestItem;

        public SingleResponse(){}

        public SingleResponse(boolean error, String errorMsg, RequestData requestItem) {
            this.error = error;
            this.errorMsg = errorMsg;
            this.requestItem = requestItem;
        }

        public boolean isError() { return error; }
        public String getErrorMsg() { return errorMsg; }
        public RequestData getRequestItem() { return requestItem; }
    }

    public static class FilterObject {
        String filter;
        public String getFilter() { return filter; }
        public void setFilter(String filter) { this.filter = filter; }
    }
}
