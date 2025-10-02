# Run this script with PowerShell to test the Uno game with UTF-8 encoding
# This will help ensure Vietnamese characters display properly

Write-Host "=== Chuẩn bị môi trường để hiển thị tiếng Việt có dấu ==="
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Compile the project
Write-Host "=== Biên dịch dự án ==="
mvn clean compile

# Check for running processes and kill them if needed
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -match "UnoServer|UnoClient" }
if ($javaProcesses) {
    Write-Host "=== Dừng các quá trình Java đang chạy ==="
    $javaProcesses | ForEach-Object { Stop-Process -Id $_.Id -Force }
    Start-Sleep -Seconds 1
}

# Start the server in a new window
Write-Host "=== Khởi động Server với UTF-8 ==="
Start-Process powershell -ArgumentList "-NoExit", "-Command", "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; cd '$PSScriptRoot'; java '-Dfile.encoding=UTF-8' -cp target/classes com.uno.server.UnoServer"

# Wait for the server to start
Write-Host "=== Đợi Server khởi động (5 giây) ==="
Start-Sleep -Seconds 5

# Start multiple clients in new windows
Write-Host "=== Khởi động Client với UTF-8 ==="
Start-Process powershell -ArgumentList "-NoExit", "-Command", "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; cd '$PSScriptRoot'; java '-Dfile.encoding=UTF-8' -cp target/classes com.uno.client.UnoClientMain"

Write-Host "=== Các ứng dụng đã được khởi động ==="
Write-Host "1. Kiểm tra các cửa sổ PowerShell để xem thông tin đăng nhập và kết nối"
Write-Host "2. Đăng nhập vào client, tạo phòng và chơi game"
Write-Host "3. Theo dõi log để đảm bảo tiếng Việt có dấu hiển thị chính xác"