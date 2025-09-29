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

### Chạy Server

```bash
# Sử dụng Maven
mvn exec:java -Dexec.mainClass="com.uno.server.UnoServer"

# Hoặc sử dụng JAR đã đóng gói
java -jar target/uno-server-jar-with-dependencies.jar
```

### Chạy Client

```bash
# Sử dụng Maven
mvn exec:java -Dexec.mainClass="com.uno.client.UnoClientMain"

# Hoặc sử dụng JAR đã đóng gói
java -jar target/uno-client-jar-with-dependencies.jar
```

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