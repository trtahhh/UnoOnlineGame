# Uno Online Game

Dự án game Uno trực tuyến sử dụng Java và kiến thức lập trình mạng máy tính. Cho phép 4 người chơi kết nối và chơi game Uno qua mạng LAN hoặc Internet.

## Mô tả

Uno Online là phiên bản mô phỏng của trò chơi bài Uno phổ biến. Game cho phép:
- Kết nối 4 người chơi thông qua mạng
- Giao diện đồ họa (GUI) đơn giản và thân thiện
- Tuân theo luật chơi Uno tiêu chuẩn
- Hỗ trợ tính năng chat trong game

## Cây thư muc cua dư an

src/
├── main/
│   └── java/
│       └── com/
│           └── uno/
│               ├── server/     # Mã nguồn máy chủ
│               ├── client/     # Mã nguồn người chơi
│               ├── model/      # Các lớp đối tượng (bài, người chơi, luật chơi)
│               ├── gui/        # Giao diện đồ họa
│               └── utils/      # Tiện ích


## Yêu cầu hệ thống

- Java JDK 17 trở lên
- Kết nối mạng LAN hoặc Internet

## Hiển thị tiếng Việt có dấu trong log

Để hiển thị đúng tiếng Việt có dấu trong log của game, bạn cần:

1. Chạy server và client với tham số `-Dfile.encoding=UTF-8`:
   ```
   java -Dfile.encoding=UTF-8 -cp target/classes com.uno.server.UnoServer
   java -Dfile.encoding=UTF-8 -cp target/classes com.uno.client.UnoClientMain
   ```

2. Nếu sử dụng PowerShell, hãy cài đặt encoding UTF-8:
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   ```

3. Nếu sử dụng Command Prompt (cmd), hãy đổi code page:
   ```
   chcp 65001
   ```

4. Nếu vẫn gặp vấn đề với font chữ, hãy đảm bảo console của bạn đang sử dụng font hỗ trợ Unicode như Consolas hoặc Lucida Console.

## Cách cài đặt và chạy

### Cài đặt Maven (nếu chưa có)

Dự án sử dụng Maven để quản lý dependency và build. Tải Maven từ trang chủ [Maven](https://maven.apache.org/download.cgi) và cài đặt theo hướng dẫn.

### Biên dịch với Maven

```bash
# Chuyển đến thư mục gốc của dự án
cd path/to/Uno

# Biên dịch dự án
mvn clean compile
```

### Đóng gói thành JAR

```bash
# Tạo các file JAR
mvn clean package
```

Sau khi đóng gói, bạn sẽ có 2 file JAR trong thư mục target:
- uno-server-jar-with-dependencies.jar: Chứa server
- uno-client-jar-with-dependencies.jar: Chứa client

### Sử dụng Visual Studio Code Tasks

Nếu bạn sử dụng Visual Studio Code, có sẵn các task để dễ dàng biên dịch và chạy dự án:

1. Mở menu Terminal > Run Task... hoặc nhấn Ctrl+Shift+P và tìm "Tasks: Run Task"
2. Chọn một trong các task sau:
   - **Biên dịch dự án**: Biên dịch mã nguồn (mvn clean compile)
   - **Đóng gói dự án**: Tạo các file JAR (mvn clean package)
   - **Chạy Server**: Khởi động máy chủ Uno
   - **Chạy Client**: Khởi động ứng dụng khách Uno
   - **Chạy Server với UTF-8**: Khởi động máy chủ với hỗ trợ UTF-8 để hiển thị đúng tiếng Việt có dấu
   - **Chạy Client với UTF-8**: Khởi động ứng dụng khách với hỗ trợ UTF-8 để hiển thị đúng tiếng Việt có dấu

### Chạy Server

```bash
# Sử dụng Maven
mvn exec:java -Dexec.mainClass="com.uno.server.UnoServer"

# Chạy từ classes với encoding UTF-8 để hiển thị đúng tiếng Việt có dấu
java -Dfile.encoding=UTF-8 -cp target/classes com.uno.server.UnoServer

# Hoặc sử dụng JAR đã đóng gói
java -Dfile.encoding=UTF-8 -jar target/uno-server-jar-with-dependencies.jar
```

### Chạy Client

```bash
# Sử dụng Maven
mvn exec:java -Dexec.mainClass="com.uno.client.UnoClientMain"

# Chạy từ classes với encoding UTF-8 để hiển thị đúng tiếng Việt có dấu
java -Dfile.encoding=UTF-8 -cp target/classes com.uno.client.UnoClientMain

# Hoặc sử dụng JAR đã đóng gói
java -Dfile.encoding=UTF-8 -jar target/uno-client-jar-with-dependencies.jar
```

## Kiểm thử và khắc phục sự cố

### Kiểm tra các quá trình Java đang chạy

Nếu gặp lỗi "Address already in use: bind" khi khởi động server, kiểm tra và dừng các quá trình Java đang chạy:

```bash
# Liệt kê tất cả các quá trình Java
jps

# Dừng một quá trình Java cụ thể (thay <pid> bằng ID của quá trình)
# PowerShell
Stop-Process -Id <pid>
# hoặc Linux/macOS
kill <pid>
```

### Kiểm tra port 5000 đã được sử dụng chưa

```bash
# Windows
netstat -ano | findstr 5000

# Linux/macOS
netstat -anp | grep 5000
```

### Kiểm tra kết nối mạng

Nếu client không thể kết nối đến server:
1. Đảm bảo server đã được khởi động
2. Kiểm tra địa chỉ IP và cổng (mặc định là port 5000)
3. Đảm bảo tường lửa không chặn kết nối
4. Thử kết nối bằng địa chỉ localhost (127.0.0.1) trước khi thử kết nối qua mạng LAN/Internet

## Script Kiểm thử Nhanh

Dự án cung cấp một script PowerShell để kiểm thử nhanh game Uno:

```powershell
# Chạy script từ thư mục gốc của dự án
.\test-uno-game.ps1
```

Script này sẽ:
1. Thiết lập môi trường PowerShell với UTF-8
2. Biên dịch dự án
3. Dừng bất kỳ quá trình Java nào đang chạy (nếu có)
4. Khởi động Server trong một cửa sổ PowerShell riêng
5. Khởi động Client trong một cửa sổ PowerShell riêng

Script này rất hữu ích để:
- Kiểm tra nhanh việc hiển thị tiếng Việt có dấu trong logs
- Kiểm thử kết nối giữa client và server
- Chạy demo trò chơi

### Chạy từ Visual Studio Code

1. Mở dự án trong VS Code
2. Chọn tab "Run and Debug" (Ctrl+Shift+D)
3. Chọn cấu hình "Run UnoServer" để chạy server
4. Chọn cấu hình "Run UnoClient" để chạy client

## Hướng dẫn sử dụng

### Đăng nhập
1. Khởi động server trước
2. Khởi động client
3. Nhập địa chỉ server (mặc định là localhost)
4. Nhập tên người chơi
5. Nhấn "Kết nối"

### Tạo và tham gia phòng
1. Trong màn hình sảnh, nhấn "Tạo phòng" để tạo phòng mới
2. Hoặc chọn một phòng trong danh sách và nhấn "Tham gia" để vào phòng có sẵn
3. Người chơi tạo phòng sẽ là chủ phòng và có thể bắt đầu game

### Chơi game
1. Chủ phòng nhấn "Bắt đầu" khi đã có đủ người chơi
2. Khi đến lượt của bạn, chọn lá bài phù hợp để đánh
3. Nếu không có lá bài phù hợp, nhấn "Rút bài"
4. Sau khi rút bài, bạn có thể đánh bài vừa rút hoặc nhấn "Kết thúc lượt"
5. Khi chỉ còn 1 lá bài, nhấn "UNO!" để hô Uno
6. Người chơi đánh hết bài đầu tiên sẽ thắng

## Quy tắc mạng

Dự án này sử dụng kiến thức lập trình mạng để kết nối các người chơi:

1. **Kiến trúc Server-Client**: Một máy chủ trung tâm quản lý nhiều kết nối client
2. **Socket Java**: Sử dụng Socket để truyền dữ liệu qua mạng
3. **Serialization**: Truyền/nhận đối tượng thông qua ObjectInputStream/ObjectOutputStream
4. **Đa luồng**: Mỗi kết nối client được xử lý bởi một thread riêng
5. **Đồng bộ hóa**: Đảm bảo tính nhất quán của dữ liệu game giữa các người chơi

## Các tính năng nâng cao

- Hệ thống phòng chơi với tối đa 4 người/phòng
- Chat trong game để người chơi giao tiếp
- Xử lý các hiệu ứng đặc biệt của lá bài (Skip, Reverse, Draw Two, Wild, Wild Draw Four)
- Hỗ trợ quy tắc hô "UNO" khi còn 1 lá bài

## Phát triển thêm

Dự án có thể được phát triển thêm với các tính năng sau:

1. Thêm âm thanh và hiệu ứng hình ảnh
2. Lưu thống kê người chơi
3. Thêm chức năng đăng ký/đăng nhập
4. Hỗ trợ chơi với AI khi không đủ người chơi
5. Cải thiện giao diện người dùng với animation

## Luật chơi

Trò chơi tuân theo luật chơi Uno tiêu chuẩn:
- Mỗi người chơi nhận 7 lá bài ban đầu
- Người chơi phải đánh lá bài cùng màu hoặc cùng số/ký hiệu với lá bài hiện tại
- Có các lá bài đặc biệt: Skip, Reverse, Draw Two, Wild, Wild Draw Four
- Người chơi phải hô "UNO" khi chỉ còn 1 lá bài
- Người chơi đầu tiên đánh hết bài sẽ thắng cuộc

## Tính năng mạng

- Kiến trúc Server-Client sử dụng Socket
- Hỗ trợ kết nối đồng thời cho 4 người chơi
- Xử lý các thông điệp game thông qua giao thức tự định nghĩa
- Đồng bộ hóa trạng thái game giữa các người chơi

## Phát triển

Dự án này được phát triển với mục đích học tập về lập trình mạng và phát triển game. Bạn có thể mở rộng dự án bằng cách:
- Thêm hiệu ứng âm thanh
- Cải thiện giao diện đồ họa
- Thêm chức năng đăng nhập/đăng ký
- Bổ sung tính năng chơi với AI