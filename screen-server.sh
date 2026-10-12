#!/system/bin/sh
# Web-based screen streaming server for Siyata
# Access at http://192.168.0.26:8080

PORT=8080
QUALITY=50
SIZE=720

echo "Starting screen streaming server on port $PORT..."
echo "Access from browser at: http://192.168.0.26:$PORT"

# Create temp directory for screenshots
mkdir -p /data/local/tmp/screen-stream

# Start HTTP server in background
(
  cd /data/local/tmp/screen-stream
  while true; do
    {
      echo -e "HTTP/1.1 200 OK\r"
      echo -e "Content-Type: multipart/x-mixed-replace; boundary=frame\r"
      echo -e "Cache-Control: no-cache\r"
      echo -e "Connection: close\r"
      echo -e "\r"

      while true; do
        # Capture screenshot
        screencap -p /data/local/tmp/screen-stream/screen.png

        # Get file size
        SIZE=$(stat -c%s /data/local/tmp/screen-stream/screen.png)

        # Send frame
        echo -e "--frame\r"
        echo -e "Content-Type: image/png\r"
        echo -e "Content-Length: $SIZE\r"
        echo -e "\r"
        cat /data/local/tmp/screen-stream/screen.png
        echo -e "\r"

        # Delay between frames (adjust for performance)
        sleep 0.5
      done
    } | nc -l -p $PORT -w 1
  done
) &

echo "Server started with PID $!"
echo "Press Ctrl+C to stop"
