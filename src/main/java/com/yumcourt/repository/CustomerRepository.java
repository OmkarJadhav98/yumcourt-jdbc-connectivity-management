package com.yumcourt.repository;

import com.yumcourt.model.Customer;
import com.yumcourt.model.Contact;
import com.project.service.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);
    private Connection connection;
    private ContactRepository contactRepository;

    public CustomerRepository() {
        this.contactRepository = contactRepository;
    }


    private void initConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = ConnectionService.getConnection();
        }
    }

    public List<Customer> retrieveCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customer";

        try {
            this.initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                int contactId = resultSet.getInt("contact_id");

                // Fetch the contact for this customer
                Contact contact = contactRepository.getContactById(contactId);

                Customer customer = new Customer(id, name, email, contact);
                customers.add(customer);
            }
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return customers;
    }

    public void createCustomer(Customer customer) {
        String query = "INSERT INTO customer (name, email, contact_id) VALUES (?, ?, ?)";
        try {
            this.initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getEmail());
            preparedStatement.setLong(3, customer.getContact().getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    public void updateCustomer(Customer customer) {
        String query = "UPDATE customer SET name = ?, email = ?, contact_id = ? WHERE id = ?";
        try {
            this.initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setString(2, customer.getEmail());
            preparedStatement.setLong(3, customer.getContact().getId());
            preparedStatement.setLong(4, customer.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    public void deleteCustomer(long id) {
        String query = "DELETE FROM customer WHERE id = ?";
        try {
            this.initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    public Customer findById(long customerId) {
        String query = "SELECT * FROM customer WHERE id = ?";
        Customer customer = null;
        try {
            this.initConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, customerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                int contactId = resultSet.getInt("contact_id");

                // Fetch the contact for this customer
                Contact contact = contactRepository.getContactById(contactId);

                customer = new Customer(id, name, email, contact);
            }
        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return customer;
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage(), e);
            }
        }
    }
}
