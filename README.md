# Overview
This tool enhances the development and debugging process by providing a user-friendly interface to interact with 
Zeebe job workers directly, facilitating the testing and validation of workflows under various conditions.

# How-To

1. Run container locally with  
    ```
    docker run --rm -it -p 26500:26500 -p 8080:8080 mityavasilyev/zeebe-dev-helper
    ```

2. After that you can send HTTP requests to zeebe-dev-helper service and trigger desired job worker </br>
   For example, to trigger job worker with type(name/code) *update_date*, 
    use path `localhost:8080/api/v1/update_date` </br>
    
   If you also want to add some variables for job worker to handle, put them in request body as simple json.
   ```
   {
    "date": "12.12.1995",
    "dateStyle": "EU"
   }
   ```

   This would make your request look like this:
   ```
   curl --location 'localhost:8080/api/v1/update_date' \
    --header 'Content-Type: application/json' \
    --data '{
        "date": "12.12.1995",
        "dateStyle": "EU"
    }'
   ```

   You can also check Postman collection in root folder for reference
