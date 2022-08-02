import java.sql.*;
import java.util.*;
import java.text.*;
import org.json.*;

public class ResourceManagerTester
{
    public static void main(String[] args)
    {
        ResourceManagerService resourceManager = new ResourceManagerService();
        //main method, use for testing
        System.out.println("try out commands here to see them in standard IO!");
    }
}
class ResourceManagerService
{
    Connection connection;
    Statement statement;
    public ResourceManagerService()
    {
        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/resourceManager", "root", "password123sql");
            statement = connection.createStatement();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /*
    Note: output are JSON objects in String representation (returns null if Exception is thrown)
     */
    public String summary(String request)
    {
        String[] parts = request.split("[?&=]");
        String nameQuery = "";
        String typeQuery = "";
        for(int i=0; i < parts.length; i++)
            if(parts[i].equals("name"))
            {
                nameQuery = parts[i+1];
                break;
            }
        for(int i=parts.length-2; i >= 0; i--)
            if(parts[i].equals("userType"))
            {
                typeQuery = parts[i+1];
                break;
            }
        try
        {
            String[] keys = {"dateOfBirth", "firstName", "userId", "lastName"};
            ResultSet resultSet = statement.executeQuery("SELECT * FROM User;");
            JSONArray result = new JSONArray();
            while(resultSet.next())
            {
                String first = resultSet.getString("firstName");
                String last = resultSet.getString("lastName");
                String type = resultSet.getString("userType");

                boolean matches = typeQuery.length() == 0 || typeQuery.equals(type);
                matches &= nameQuery.length() == 0 || nameQuery.equals(first) || nameQuery.equals(last);

                if(matches)
                {
                    JSONObject person = new JSONObject();
                    for(String key: keys)
                    {
                        if(key.equals("userId"))
                            person.put(key, resultSet.getInt(key));
                        else
                            person.put(key, resultSet.getString(key));
                    }
                    result.put(person);
                }
            }
            return result.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public String getUser(String request)
    {
        //assumes id isn't missing in request
        String[] data = request.split("/");
        int id = Integer.parseInt(data[data.length-1]);
        //returns null if no user found
        try
        {
            String[] keys = {"userId", "firstName", "lastName", "annualSalary", "dateOfBirth", "email", "fullName", "gender", "mobilePhone", "userType"};
            ResultSet resultSet = statement.executeQuery("SELECT * FROM User;");
            while(resultSet.next())
                if(resultSet.getInt("userId") == id)
                {
                    JSONObject result = new JSONObject();
                    for(String key: keys)
                    {
                        if(key.equals("userId"))
                            result.put(key, resultSet.getInt(key));
                        else
                            result.put(key, resultSet.getString(key));
                    }
                    return result.toString();
                }
            return null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public String getAddressesForUser(String request)
    {
        //assumes id isn't missing in request
        String[] data = request.split("/");
        int id = Integer.parseInt(data[data.length-1]);
        try
        {
            String[] keys = {"addressId", "userId", "addrLn1", "addrName", "addrType", "city", "stateCode", "postalCode", "country"};
            ResultSet resultSet = statement.executeQuery("SELECT * FROM UserAddress;");
            JSONArray result = new JSONArray();
            while(resultSet.next())
            {
                int ownerId = resultSet.getInt("userId");
                if(id == ownerId)
                {
                    JSONObject address = new JSONObject();
                    for(String key: keys)
                    {
                        if(key.length() >= 2 && key.substring(key.length()-2).equals("Id"))
                            address.put(key, resultSet.getInt(key));
                        else
                            address.put(key, resultSet.getString(key));
                    }
                    result.put(address);
                }
            }
            return result.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    //returns newUser JSON if query successful, error messages JSON otherwise
    public String addUser(String newUser)
    {
        String[] attributes = {"firstName", "lastName", "annualSalary", "dateOfBirth", "email", "gender", "mobilePhone", "userType"};
        /*
        INSERT INTO User (userId, firstName, lastName, annualSalary, dateOfBirth, email, gender, mobilePhone, userType)
        VALUES
        (null, 'Bo', 'Bees', '420000.88', '2000-01-30', 'pizzahut@gmail.com', 'male', '5711239999', 'employee')
         */
        StringBuilder query = new StringBuilder("INSERT INTO User (userId, ");
        for(int i=0; i < attributes.length; i++)
        {
            query.append(attributes[i]);
            if(i+1 < attributes.length)
                query.append(", ");
            else
                query.append(")");
        }
        query.append(" VALUES (null, ");
        JSONObject json = new JSONObject(newUser);
        ArrayList<String> missing = new ArrayList<String>();
        ArrayList<String> badFormat = new ArrayList<String>();
        for(int i=0; i < attributes.length; i++)
        {
            String value = (String)json.get(attributes[i]);
            if(value == null)
                missing.add(attributes[i]);
            else
            {
                //DOB and email checks
                if(attributes[i].equals("email") && !validEmail(value))
                    badFormat.add("email");
                if(attributes[i].equals("dateOfBirth") && !validDOB(value))
                    badFormat.add("dateOfBirth");

                query.append(value);
                if(i+1 < attributes.length)
                    query.append(", ");
                else
                    query.append(")");
            }
        }
        //return error JSON if missing or invalid arguments exist
        if(Math.max(missing.size(), badFormat.size()) > 0)
        {
            int K = missing.size()+badFormat.size();
            String[] messages = new String[K];
            int index = 0;
            for(String s: missing)
                messages[index++] = "Please enter "+s;
            for(String s: badFormat)
                messages[index++] = "Invalid format for "+s;
            JSONObject errorMessages = new JSONObject();
            errorMessages.put("status", 400);
            errorMessages.put("messages", messages);
            return errorMessages.toString();
        }
        try
        {
            statement.execute(query.toString()+";");
            return json.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "Exception Thrown";
        }
    }
    public String addUserAddress(String newAddress)
    {
        /*
        INSERT INTO UserAddress (addressId, addrLn1, addrName, addrType, city, stateCode, postalCode, country, userId)
        VALUES
        (null, '1234 Main Street', 'Home', 'Shipping', 'Gatlinburg', '20', '56789', 'USA', null)
         */
        String[] attributes = {"addrLn1", "addrName", "addrType", "city", "stateCode", "postalCode", "country"};
        StringBuilder query = new StringBuilder("INSERT INTO User (addressId, ");
        for(int i=0; i < attributes.length; i++)
        {
            query.append(attributes[i]);
            query.append(", ");
        }
        query.append("null) VALUES (null, ");
        ArrayList<String> missing = new ArrayList<String>();
        JSONObject json = new JSONObject(newAddress);
        for(int i=0; i < attributes.length; i++)
        {
            String value = (String)json.get(attributes[i]);
            if(value == null)
                missing.add(attributes[i]);
            else
            {
                query.append(value);
                query.append(", ");
            }
        }
        query.append("null)");
        if(missing.size() > 0)
        {
            int K = missing.size();
            String[] messages = new String[K];
            int index = 0;
            for(String s: missing)
                messages[index++] = "Please enter "+s;
            JSONObject errorMessages = new JSONObject();
            errorMessages.put("status", 400);
            errorMessages.put("messages", messages);
            return errorMessages.toString();
        }
        try
        {
            statement.execute(query.toString()+";");
            return json.toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "Exception Thrown";
        }
    }
    //returns true if user previously existed in table and was successfully deleted, false otherwise
    public boolean deleteUser(String request)
    {
        //assumes id isn't missing in request
        String[] data = request.split("/");
        int userId = Integer.parseInt(data[data.length-1]);
        StringBuilder deleteAddressesCommand = new StringBuilder("DELETE FROM UserAddress WHERE userId = "+userId+";");
        try
        {
            statement.execute(deleteAddressesCommand.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        StringBuilder deleteUserCommand = new StringBuilder("DELETE FROM User WHERE userId = "+userId+";");
        try
        {
            if(!statement.execute(deleteAddressesCommand.toString()))
                return false;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean validDOB(String dob)
    {
        /*
        String[] dates = dob.split("-");
        if(dates.length != 3)
            return false;
        String ePattern = "[0-9]+";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        for(String str: dates)
        {
            java.util.regex.Matcher m = p.matcher(str);
            if(!m.matches())
                return false;
        }
         */
        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            df.parse(dob);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    private boolean validEmail(String email)
    {
        //copied from somewhere
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
