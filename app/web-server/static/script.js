const img = document.getElementById('bitmap');
let lastFetchedBitmap = null; // Cache for the last received bitmap
let continueUpdatingBitmap = true; // Control for updates
let numberOfUnchangedFrames = 0; // Counter for unchanged frames

const webSocket = new WebSocket('ws://127.0.0.1:3030/ws'); // Replace with your WebSocket URL
webSocket.binaryType = 'arraybuffer'; // Set to receive binary data

const isProduction = window.location.hostname !== 'localhost';
function getTimestamp() {
    return `[${new Date().toISOString()}]`;
}

// Log messages with timestamps
function logMessage(message, level = 'info', error = '', metadata = {}) {
    if (!message || (isProduction && level === 'debug')) {
        return;
    }
    const timestamp = getTimestamp();
    const metaString = Object.keys(metadata).length ? JSON.stringify(metadata) : '';
    console[level](`${timestamp} ${level.toUpperCase()}: ${message} ${metaString}`, error || '');
}

// When the connection is established
webSocket.onopen = () => {
    logMessage('WebSocket connection established', 'info');
};

// Handle messages received from the server
webSocket.onmessage = (event) => {
    if (!continueUpdatingBitmap) {
        logMessage('Bitmap update paused', 'info');
        return;
    }

    try {
        const arrayBuffer = event.data; // Received binary data
        const newBitmap = new Uint8Array(arrayBuffer); // Convert to Uint8Array

        // Convert to string for comparison
        const newBitmapString = newBitmap.toString();

        // Check if the bitmap has changed
        if (newBitmapString === lastFetchedBitmap) {
            logMessage('Bitmap unchanged. Skipping update.', 'info');
            numberOfUnchangedFrames++;
            if (numberOfUnchangedFrames >= 10) {
                logMessage('Bitmap unchanged for 10 frames. Stopping updates.', 'info');
                continueUpdatingBitmap = false;
            }
            return;
        }

        // Update bitmap cache
        lastFetchedBitmap = newBitmapString;

        // Create a Blob and update the image
        const blob = new Blob([arrayBuffer], { type: 'image/bmp' });
        const url = URL.createObjectURL(blob);
        img.src = url;

        numberOfUnchangedFrames = 0; // Reset the counter for unchanged frames
    } catch (error) {
        logMessage('Error processing bitmap:', 'error', error);
    }
};

// Handle connection closure
webSocket.onclose = () => {
    logMessage('WebSocket connection closed', 'info');
};

// Handle connection errors
webSocket.onerror = (error) => {
    logMessage('WebSocket error occurred:', 'error', error);
    continueUpdatingBitmap = false;
};

document.getElementById('restartButton').addEventListener('click', async () => {
    try {
        lastFetchedBitmap = null;
        numberOfUnchangedFrames = 0;
        continueUpdatingBitmap = true;

        const response = await fetch('http://localhost:3030/api/restart', {
            method: 'POST', // Use POST or GET based on your API design
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ action: "buttonClicked" }) // Replace with relevant data
        });

        if (!response.ok) {
            throw new Error(`Server responded with status: ${response.status}`);
        }

        const data = await response.json();
        logMessage('Server Response:', 'info', data);
    } catch (error) {
        logMessage('Error communicating with server:', 'error', error);
    }
});

document.getElementById('selectObj').addEventListener('change', (event) => {
    const file = event.target.value; // Get the selected file
    if (file) {
        logMessage('Selected file:' + file, 'info'); // Log the file name
        // You can also upload the file to the server here

        fetch('/api/upload', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain' // Indicate raw text content
            },
            body: file
        })
        .then(response => response.text())
        .then(data => logMessage('Server Response:' + data, 'info'))
        .catch(error => logMessage('Error uploading file:' + error, 'error'));
    }
});
