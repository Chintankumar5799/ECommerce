const axios = require('axios');
const FormData = require('form-data');
async function run() {
    try {
        const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPT09AbWFpbC5jb20iLCJyb2xlcyI6WyJST0xFX0JVWUVSIl0sInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3NzI2MDQzMTUsImV4cCI6MTc3MjYwNDkxNX0._z-3E0m6ZWXXWj0aZb-YcykMXFOsACLX3Sv8MrxHc0s";

        const form = new FormData();
        const productRequest = {
            productName: "Test Product from CLI",
            subCategoryId: null // Intentionally null to hit validation logic
        };
        form.append('productRequest', JSON.stringify(productRequest), {
            contentType: 'application/json'
        });
        form.append('jsonAttributes', JSON.stringify({ color: "blue" }));

        const authHeaders = {
            'Authorization': 'Bearer ' + token,
            ...form.getHeaders()
        };

        const resProd = await axios.post('http://localhost:8081/api/product/newProduct', form, {
            headers: authHeaders
        });

        console.log("Product Status:", resProd.status);
        console.log("Product Data:", resProd.data);

    } catch (error) {
        if (error.response) {
            console.log("Error Status:", error.response.status);
            console.log("Error Data:", JSON.stringify(error.response.data, null, 2));
        } else {
            console.log("Error Details:", error.message);
        }
    }
}
run();
