const img = document.getElementById('bitmap');
let lastFetchedBitmap = null; // Cache for the last fetched bitmap data
let continueUpdatingBitmap = true;
let animationFrameId = null; // To track the requestAnimationFrame ID
let numberOfUnchangedFrames = 0;

// Throttle function to limit fetch requests
function throttle(func, delay, { leading = true, trailing = true } = {}) {
    let timeoutId = null;
    let lastCall = 0;
    let lastArgs = null;
    let lastThis = null;

    const execute = () => {
        func.apply(lastThis, lastArgs);
        lastCall = Date.now();
        lastArgs = null;
        lastThis = null;
    };

    const throttled = function (...args) {
        const now = Date.now();
        if (!leading && !lastCall) {
            lastCall = now; // Prevents leading execution if disabled
        }

        const remaining = delay - (now - lastCall);
        lastArgs = args;
        lastThis = this;

        if (remaining <= 0 || remaining > delay) {
            if (timeoutId) {
                clearTimeout(timeoutId);
                timeoutId = null;
            }

            if (leading || !timeoutId) {
                execute();
            }
        } else if (trailing && !timeoutId) {
            timeoutId = setTimeout(() => {
                timeoutId = null;
                if (trailing && lastArgs) {
                    execute();
                }
            }, remaining);
        }
    };

    throttled.cancel = () => {
        clearTimeout(timeoutId);
        timeoutId = null;
        lastArgs = null;
        lastThis = null;
    };

    return throttled;
}

const isProduction = window.location.hostname !== 'localhost';

function getTimestamp() {
    return `[${new Date().toISOString()}]`;
}

// Log messages with timestamps
function logMessage(message, level = 'info', error = '', metadata = {}) {
    if (!message || (isProduction && level === 'debug')) return;

    const timestamp = getTimestamp();
    const metaString = Object.keys(metadata).length ? JSON.stringify(metadata) : '';

    console[level](`${timestamp} ${level.toUpperCase()}: ${message} ${metaString}`, error || '');
}

// Function to update the bitmap
async function updateBitmap() {
    if (!continueUpdatingBitmap) {
        logMessage('Bitmap update paused', 'info');
        return;
    }
    if (!navigator.onLine) {
        continueUpdatingBitmap = false;
        logMessage('Network offline: Cannot fetch bitmap', 'error');
        if (img.src !== '/static/placeholder.png') {
            img.src = '/static/placeholder.png';
        }
        return;
    }

    try {
        const response = await fetch('/mobilert_bitmap');
        if (!response.ok) {
            continueUpdatingBitmap = false;
            logMessage(`Error fetching bitmap: ${response.status} ${response.statusText}`, 'error');
            if (img.src !== '/static/placeholder.png') {
                img.src = '/static/placeholder.png';
            }
            return;
        }

        const newBitmap = await response.text();

        // Check if the bitmap has changed
        if (newBitmap === lastFetchedBitmap) {
            logMessage('Bitmap unchanged. Skipping update.', 'info');
            numberOfUnchangedFrames++;
            if (numberOfUnchangedFrames >= 10) {
                logMessage('Bitmap unchanged for 10 frames. Stopping updates.', 'info');
                continueUpdatingBitmap = false;
            }
            return;
        }

        const newSrc = `data:image/bmp;base64,${newBitmap}`;
        img.src = newSrc;
        lastFetchedBitmap = newBitmap; // Update the cache
    } catch (error) {
        continueUpdatingBitmap = false; // Stop updating due to fetch failure
        logMessage('Error updating bitmap:', 'error', error);
        if (img.src !== '/static/placeholder.png') {
            img.src = '/static/placeholder.png';
        }
    }
}

// Throttled update function
const throttledUpdate = throttle(updateBitmap, 100);

// Animation loop
function animationLoop() {
    if (!continueUpdatingBitmap) {
        logMessage('Stopping animation loop.', 'info');
        if (animationFrameId !== null) {
            cancelAnimationFrame(animationFrameId); // Stop the scheduled frames
            animationFrameId = null;
        }
        return; // Stop the loop
    }
    throttledUpdate();
    animationFrameId = requestAnimationFrame(animationLoop); // Schedule the next frame
}

// Start the animation loop
animationFrameId = requestAnimationFrame(animationLoop);
document.getElementById('restartButton').addEventListener('click', async () => {
    try {
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

        lastFetchedBitmap = null;
        numberOfUnchangedFrames = 0;
        continueUpdatingBitmap = true;
        animationFrameId = requestAnimationFrame(animationLoop);
    } catch (error) {
        logMessage('Error communicating with server:', 'error', error);
    }
});

document.getElementById('selectObj').addEventListener('change', (event) => {
    const file = event.target.value; // Get the selected file
    if (file) {
        console.log('Selected file:', file); // Log the file name
        // You can also upload the file to the server here

        fetch('/api/upload', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain' // Indicate raw text content
            },
            body: file
        })
        .then(response => response.text())
        .then(data => console.log('Server Response:', data))
        .catch(error => console.error('Error uploading file:', error));
    }
});
