package com.example.gestinfo;

import com.salesforce.rest.SalesforceClient;
import com.salesforce.rest.User;

public class SalesforceConnector {

    public static void main(String[] args) {

        // Reemplaza con tus credenciales de Salesforce
        String username = "tuNombreDeUsuario";
        String password = "tuContraseña";
        String securityToken = "tuTokenDeSeguridad";
        String endpoint = "https://tuOrganizacion.salesforce.com";

        // Conectarse a Salesforce
        SalesforceClient client = new SalesforceClient(endpoint, username, password, securityToken);

        // Consultar datos de usuario
        String query = "SELECT Id, Name, Email FROM User";
        List<User> users = client.query(query, User.class);

        // Procesar los resultados de la consulta
        for (User user : users) {
            System.out.println("ID: " + user.getId());
            System.out.println("Nombre: " + user.getName());
            System.out.println("Correo electrónico: " + user.getEmail());
            System.out.println("----");
        }
    }
}
