const fs = require('fs');
const crypto = require('crypto');

// Load the public key
const publicKey = fs.readFileSync('public_key.pem', 'utf8');

// Sample data and signature from the server
const data = {
    flightNumber: "UA123",
    origin: "SFO",
    destination: "JFK",
    departureTime: "2025-05-10T09:00:00Z",
    arrivalTime: "2025-05-10T16:00:00Z",
    status: "Delayed",
    door: "A1",
    gate: "Gfvdssvs",
    terminal: "T1"
};
const signature = "IoZeGFyN98/gKVJ1lVEoGCxKD+V4qttCCNvRPpi7J1MsbtE8UY6IThANb6DRRIJ+yfsFucHex+RkRWHjMmggvRHKaHahUHqNBZYxA/6YOgNQh5C2Kj+7PM9cY46ZXghSeyWwh36ucg/5SqnvkNfBzlC/NW2NYIoLWTq9OJK/Btv+lPFlsTirM94CLl4o8rveRw3/aSS2hr5sKVN3YwIifb96NFmbl7/4JCjionFxiCrnaowflYgBGiU044/0o/f0oInNrNEr0W9GBIadyvn1YgfEXF8ioazl7+qTLsRT0nGboaSt6kwW7EX4apKy+Pp8yH3AvJgKFjsQSfcSaKY80Q==";  // Replace this with the signature from the server

// Verify the signature
function verifyData(data, signature) {
    const verify = crypto.createVerify('SHA256');
    verify.update(JSON.stringify(data)); // Stringify the data before verifying
    verify.end();
    return verify.verify(publicKey, signature, 'base64');
}

const isValid = verifyData(data, signature);
console.log('Signature Valid:', isValid);  // Should print 'true' if the signature is valid
