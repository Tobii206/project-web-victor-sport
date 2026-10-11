# Rà soát độ dễ hiểu của code VictorSport

Đã rà mã nguồn chính, giao diện, cấu hình, database và mã kiểm tra trong dự án. Không tính tệp biên dịch trong `target`, ảnh và thư viện tải về. Đây là rà soát tĩnh về độ dễ đọc, không phải xác nhận mọi chức năng đã hoàn thiện hay đã chạy với database thật.

## Phạm vi

| Nhóm | Số tệp | Nhận xét |
| --- | ---: | --- |
| Controller | 7 | Chủ yếu nhận dữ liệu, gọi service, trả trang hoặc chuyển hướng. |
| Service | 6 | SQL, thanh toán và giảm giá là phần cần hiểu kỹ nhất. |
| DTO | 9 | Class, thuộc tính, constructor, getter và vòng lặp cơ bản. |
| Entity | 28 | Khai báo bảng và quan hệ JPA; không có phương thức xử lý nghiệp vụ. |
| Cấu hình Security và lớp khởi động | 2 | Cấu hình Spring, có lambda ngắn. |
| JSP | 16 | HTML, JSTL và biểu thức EL; không thấy Java scriptlet nhúng. |
| JavaScript | 2 | Thao tác DOM, sự kiện click/change, if và vòng lặp. |
| CSS | 2 | Flex, Grid và media query; không dùng framework giao diện. |
| Kiểm tra Java | 8 | JUnit, Mockito và MockMvc; khó hơn code giao diện, nằm riêng trong test. |
| Database, Maven, cấu hình ứng dụng | 4 | Hai tệp SQL, pom.xml và application.properties. |

## Những phần nên làm dễ đọc hơn

1. **BanHangTaiQuayService.java — ưu tiên cao.** Tệp dài 709 dòng, chứa tìm sản phẩm, khách hàng, giảm giá, lưu/hủy đơn chờ và thanh toán. Phương thức thanh toán từ dòng 330 thực hiện nhiều bước. Nên tách thành các hàm nhỏ theo thứ tự: kiểm tra hàng, tính tiền, lưu hóa đơn, lưu chi tiết, cập nhật tồn kho, lưu thanh toán. Phần tạo khóa hóa đơn ở dòng 375 dùng `GeneratedKeyHolder` và `PreparedStatement`; có thể thống nhất cách `OUTPUT INSERTED.id` đã dùng cho đơn chờ và đơn online để bớt đoạn gán tham số thủ công.

2. **SanPhamOnlineController.java và SanPhamOnlineService.java — ưu tiên vừa.** Dùng nhiều `Map<String, Object>` và phép ép kiểu. Đọc dữ liệu bằng tên cột chuỗi dễ nhầm; giỏ trong session còn có `@SuppressWarnings("unchecked")` ở dòng 109. Nên dùng class dữ liệu có tên rõ khi đơn giản hóa tiếp, đồng thời đổi các tên ngắn `pb`, `ds`, `tb`, `kh` thành tên đầy đủ. Tách các câu lệnh cùng dòng để dễ theo dõi.

3. **JSP và CSS — ưu tiên vừa.** Các trang giỏ hàng, thanh toán, tài khoản, sản phẩm và trang chủ có nhiều dòng trên 200 ký tự. Kỹ thuật dùng vẫn cơ bản, nhưng cách trình bày khó đọc. Nên xuống dòng cho từng thẻ và từng nhánh điều kiện, thống nhất phần đầu/cuối trang dùng chung. Nhiều quy tắc CSS cũng đang dồn vào một dòng.

4. **Đăng nhập và cấu hình — mức vừa.** Đăng nhập cần phân biệt tài khoản, xử lý phiên truy cập, đếm lần sai và kiểm tra mật khẩu. Có lambda trong cấu hình Security và khi đọc SQL; đây là chỗ dùng thư viện Spring, không phải toàn bộ dự án đã loại bỏ lambda.

5. **SQL — mức vừa đến khó.** Có JOIN nhiều bảng, truy vấn con lấy ảnh, CASE trạng thái, BigDecimal tính tiền và các lệnh khóa dữ liệu. Nên giải thích theo từng bước nghiệp vụ, tránh gộp thêm nhiều việc vào cùng một phương thức.

## Những phần cần giữ

- `@Transactional`: giúp các bước lưu hóa đơn và cập nhật tồn kho cùng thành công hoặc cùng hoàn tác khi lỗi.
- Khóa dữ liệu và cập nhật tồn kho có điều kiện: bảo vệ số lượng khi nhiều người thao tác cùng lúc.
- Kiểm tra chủ sở hữu đơn chờ, trạng thái đơn, số lượng và giới hạn 10 đơn.
- `BigDecimal` để tính tiền và BCrypt để kiểm tra mật khẩu đã mã hóa.
- Kiểm tra đăng nhập trong controller. Cấu hình Security hiện cho request đi qua để controller xử lý phiên đăng nhập của website.

Không nên bỏ các phần này chỉ để giảm số dòng. Có thể thêm giải thích và chia hàm để dễ hiểu hơn.

## Phần đã đơn giản

Không thấy Stream API, Optional, record, xử lý bất đồng bộ tự viết hoặc framework JavaScript trong mã nguồn chính. DTO, xử lý ảnh, giỏ tại quầy và JavaScript chủ yếu dùng class, if/else, List/Map và vòng lặp thông thường.

Entity hiện là phần khai báo JPA, còn service thực hiện truy vấn bằng JdbcTemplate; không thấy repository JPA trong mã nguồn chính. Dự án có hai cách biểu diễn dữ liệu: DTO có kiểu rõ và Map theo tên cột, nên người đọc cần phân biệt.

## Lưu ý về phạm vi chức năng

MenuController hiện trả trang `dang-phat-trien` cho các mục quản lý chưa có controller riêng. Trang quản lý hóa đơn có controller riêng. Giỏ online hiện nằm trong session; các bảng `gio_hang` và `chi_tiet_gio_hang` chưa được service online sử dụng. Những phần này chưa thể coi là hoàn thiện chỉ vì menu đã hiển thị.

## Kết quả

Code chủ yếu dùng kiến thức cơ bản của Java và Spring, nhưng chưa thể gọi toàn bộ là code đơn giản cho người mới. Nên ưu tiên làm rõ service bán hàng tại quầy, các class dữ liệu online và cách xuống dòng JSP/CSS. Rà soát này không thay đổi chức năng hay dữ liệu; không chạy lại kiểm tra vì không sửa mã thực thi.
