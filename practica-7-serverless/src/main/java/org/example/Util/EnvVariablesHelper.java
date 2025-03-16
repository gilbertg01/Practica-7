package org.example.Util;

public class EnvVariablesHelper {

    public enum EnvironmentKeys {
        REQUEST_TABLE("REQUEST_TABLE_NAME");

        private String value;
        EnvironmentKeys(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static String getRequestsTableName(){
        return System.getenv(EnvironmentKeys.REQUEST_TABLE.getValue());
    }
}
