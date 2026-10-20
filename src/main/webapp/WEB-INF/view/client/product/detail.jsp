<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi"><head>
    <link rel="icon" type="image/svg+xml" href="/logo/favicon-victorsport.svg?v=1"><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${sanPham.ten_san_pham}" default="Chi tiết sản phẩm"/> — VictorSport</title>
<link rel="stylesheet" href="/css/trang-chu.css?v=gia-giam-1"><link rel="stylesheet" href="/css/giao-dien-chung.css?v=20261018-2"></head>
<body><%@ include file="/WEB-INF/view/client/layout/header.jsp" %>
<main class="store-container online-container">
    <p class="online-breadcrumb"><a href="/">Trang chủ</a> / <a href="/#san-pham">Sản phẩm</a> / <c:out value="${sanPham.ten_san_pham}"/></p>
    <c:if test="${not empty error}"><div class="account-error" role="alert" data-vs-flash="error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty success}"><div class="online-success" role="status" data-vs-flash="success"><c:out value="${success}"/> <a href="/gio-hang">Xem giỏ hàng →</a></div></c:if>
    <c:if test="${not empty sanPham}">
    <div class="online-detail">
        <section class="online-gallery account-card">
            <img id="anhChinh" class="online-main-image" src="/images/giay-mac-dinh.svg" alt="<c:out value='${sanPham.ten_san_pham}'/>" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
        </section>
        <section class="account-card online-product-info">
            <p class="eyebrow"><c:out value="${sanPham.ten_thuong_hieu}"/></p>
            <h1><c:out value="${sanPham.ten_san_pham}"/></h1>
            <p class="subtle">Mã sản phẩm: <c:out value="${sanPham.ma_san_pham}"/></p>
            <p class="online-price"><span id="giaBan">Chọn mẫu giày</span> <del id="giaGoc" hidden></del></p>
            <p id="tonKho" class="subtle">Chọn kích thước và màu để xem tồn kho.</p>
            <form method="post" action="/san-pham/${sanPham.id}/chon" id="formChonGiay">
                <fieldset class="online-variants"><legend>Kích thước · Màu sắc · Dáng giày
                    <button type="button" id="moHuongDanSize" class="size-guide-button" aria-haspopup="dialog" aria-controls="huongDanSize">Hướng dẫn chọn size</button>
                </legend>
                    <c:set var="daChon" value="false"/>
                    <c:forEach items="${phienBan}" var="pb">
                        <c:set var="anhMau" value="${pb.anh}"/>
                        <c:if test="${empty anhMau}"><c:set var="anhMau" value="/images/giay-mac-dinh.svg"/></c:if>
                        <c:if test="${not fn:startsWith(anhMau, '/') and not fn:startsWith(anhMau, 'http')}"><c:set var="anhMau" value="/${anhMau}"/></c:if>
                        <label class="online-variant">
                            <input type="radio" name="idPhienBan" value="${pb.id}" data-gia="${pb.gia_sau_giam}" data-gia-goc="${pb.gia_goc}" data-ton="${pb.so_luong}" data-anh="<c:out value='${anhMau}'/>" required
                                <c:if test="${pb.so_luong <= 0}">disabled</c:if>
                                <c:if test="${pb.so_luong > 0 and not daChon}">checked<c:set var="daChon" value="true"/></c:if>>
                            <span><strong><c:out value="${pb.ten_kich_thuoc}"/></strong><small><c:out value="${pb.ten_mau_sac}"/> · <c:out value="${pb.ten_form_chan}"/></small><c:if test="${pb.so_luong <= 0}"><small>Hết hàng</small></c:if></span>
                        </label>
                    </c:forEach>
                </fieldset>
                <c:if test="${empty phienBan}"><p>Sản phẩm hiện chưa có mẫu để chọn.</p></c:if>
                <div class="online-purchase">
                    <div class="online-quantity"><button type="button" id="giamSoLuong" aria-label="Giảm số lượng">−</button><input type="number" id="soLuong" name="soLuong" value="1" min="1" aria-label="Số lượng" required><button type="button" id="tangSoLuong" aria-label="Tăng số lượng">+</button></div>
                    <button class="store-button" name="thaoTac" value="them" <c:if test="${not daChon}">disabled</c:if>>Thêm vào giỏ</button>
                    <button class="store-button online-buy-now" name="thaoTac" value="mua" <c:if test="${not daChon}">disabled</c:if>>Mua ngay</button>
                </div>
            </form>
            <div class="online-notes"><p>Chọn đúng size và màu trước khi đặt hàng.</p></div>
        </section>
    </div>
    <section class="account-card online-description">
        <div class="online-info-tabs" aria-label="Thông tin và chính sách sản phẩm">
            <button type="button" class="online-info-tab active" data-noi-dung="moTaSanPham" aria-controls="moTaSanPham" aria-pressed="true">Mô tả</button>
            <button type="button" class="online-info-tab" data-noi-dung="chinhSachDoiTra" aria-controls="chinhSachDoiTra" aria-pressed="false">Chính sách đổi trả</button>
        </div>
        <div id="moTaSanPham" class="online-info-content">
        <h2>Thông tin sản phẩm</h2>
        <h3><a href="/?thuongHieu=${sanPham.id_thuong_hieu}#san-pham"><c:out value="${sanPham.ten_san_pham}"/></a></h3>
        <p><strong>Thương hiệu:</strong> <c:out value="${sanPham.ten_thuong_hieu}"/></p>
        <p><strong>Mã sản phẩm:</strong> <c:out value="${sanPham.ma_san_pham}"/></p>
        <p><strong>Chất liệu:</strong> <c:choose><c:when test="${not empty sanPham.ten_chat_lieu}"><c:out value="${sanPham.ten_chat_lieu}"/></c:when><c:otherwise>Đang cập nhật</c:otherwise></c:choose></p>
        <c:if test="${not empty sanPham.ten_xuat_xu}"><p><strong>Xuất xứ:</strong> <c:out value="${sanPham.ten_xuat_xu}"/></p></c:if>
        <c:if test="${not empty sanPham.ten_vi_tri}"><p><strong>Nhu cầu / vị trí thi đấu:</strong> <c:out value="${sanPham.ten_vi_tri}"/></p></c:if>
        <c:if test="${not empty sanPham.ten_phong_cach}"><p><strong>Phong cách:</strong> <c:out value="${sanPham.ten_phong_cach}"/></p></c:if>
        <c:if test="${not empty sanPham.ten_co_giay}"><p><strong>Cổ giày:</strong> <c:out value="${sanPham.ten_co_giay}"/></p></c:if>
        <p><strong>Tình trạng:</strong> <c:choose><c:when test="${tongTonKho > 0}">Còn hàng</c:when><c:otherwise>Hết hàng</c:otherwise></c:choose></p>
        <p class="online-authentic"><strong>Cam kết của VictorSport: Chính hãng 100%</strong></p>
        <c:choose>
            <c:when test="${not empty sanPham.mo_ta_chi_tiet}"><p><c:out value="${sanPham.mo_ta_chi_tiet}"/></p></c:when>
            <c:when test="${not empty sanPham.mo_ta_ngan}"><p><c:out value="${sanPham.mo_ta_ngan}"/></p></c:when>
        </c:choose>
        </div>
        <div id="chinhSachDoiTra" class="online-info-content" hidden>
            <p><strong>VictorSport luôn trân trọng sự tín nhiệm của quý khách dành cho chúng tôi. Chính vì vậy, chúng tôi luôn cố gắng mang đến quý khách những sản phẩm chất lượng cao và tiết kiệm chi phí.</strong></p>
            <p>Thay cho cam kết về chất lượng sản phẩm, VictorSport thực hiện chính sách đổi trả hàng hóa. Theo đó, tất cả các sản phẩm được mua tại VictorSport đều có thể đổi size và mẫu trong vòng 07 ngày sau khi nhận hàng.</p>
            <p>Để được thực hiện đổi hàng hóa, quý khách cần giữ lại hóa đơn mua hàng tại VictorSport. Sản phẩm được đổi phải đáp ứng các điều kiện trong chính sách đổi trả hàng hóa.</p>
            <p><strong>VictorSport thực hiện đổi hàng hoặc trả lại tiền cho quý khách, nhưng không hoàn lại phí vận chuyển hoặc lệ phí giao hàng, trừ những trường hợp sau:</strong></p>
            <ul>
                <li>Không đúng chủng loại, mẫu mã như quý khách đặt hàng.</li>
                <li>Tình trạng bên ngoài bị ảnh hưởng như bong tróc, bể vỡ xảy ra trong quá trình vận chuyển.</li>
                <li>Không đạt chất lượng: phát hiện hàng giả, hàng kém chất lượng, không phải hàng chính hãng.</li>
            </ul>
            <p>Quý khách vui lòng kiểm tra hàng hóa và ký nhận tình trạng với nhân viên giao hàng ngay khi nhận hàng. Khi phát hiện một trong các trường hợp trên, quý khách có thể trao đổi trực tiếp với nhân viên giao hàng hoặc phản hồi cho VictorSport trong vòng 24 giờ qua hotline: <a href="tel:0325327602">0325327602</a>.</p>
            <p><strong>VictorSport sẽ không chấp nhận đổi trả hàng khi:</strong></p>
            <ul>
                <li>Hàng hóa là hàng đặt trước (order).</li>
                <li>Thời điểm thông báo đổi trả quá 07 ngày kể từ khi quý khách nhận hàng.</li>
                <li>Quý khách tự làm ảnh hưởng tình trạng bên ngoài như rách bao bì, bong tróc, bể vỡ, bị bẩn, hư hại, không còn nguyên vẹn như ban đầu.</li>
                <li>Quý khách sử dụng không đúng chỉ dẫn gây hỏng hóc hàng hóa.</li>
                <li>Quý khách đã kiểm tra và ký nhận tình trạng hàng hóa nhưng không phản hồi trong vòng 24 giờ kể từ lúc ký nhận hàng.</li>
                <li>Không còn size hoặc mẫu mà khách hàng muốn đổi.</li>
                <li>Đổi từ hàng hóa có sẵn sang hàng phải đặt trước.</li>
                <li>Sản phẩm đã cắt tag hoặc mác.</li>
                <li>Sản phẩm đã qua sử dụng.</li>
            </ul>
            <p><strong>VictorSport thực hiện đổi trả theo quy trình sau:</strong></p>
            <ol>
                <li><strong>Bước 1:</strong> Quý khách liên hệ trực tiếp với VictorSport qua hotline <a href="tel:0325327602">0325327602</a> để thông báo tình trạng hàng hóa cần đổi trả trong vòng 07 ngày kể từ khi nhận hàng.</li>
                <li><strong>Bước 2:</strong> Nhân viên VictorSport tiếp nhận phản hồi và hướng dẫn quý khách cung cấp thông tin đơn hàng để kiểm tra.</li>
                <li><strong>Bước 3:</strong> Quý khách gửi hàng cần đổi trả kèm hóa đơn về cửa hàng VictorSport tại Cầu Giấy, theo hướng dẫn của nhân viên, để chúng tôi kiểm tra.</li>
                <li><strong>Bước 4:</strong> Sau khi kiểm tra và xác nhận sản phẩm đủ điều kiện đổi trả, VictorSport liên hệ lại với quý khách để xác nhận và gửi sản phẩm đổi về địa chỉ quý khách cung cấp.</li>
            </ol>
            <p class="online-return-note"><em><strong>Lưu ý:</strong> Quý khách chịu phí vận chuyển hai chiều khi đổi trả. Chỉ hỗ trợ đổi sản phẩm một lần duy nhất.</em></p>
        </div>
    </section>
    <section class="online-related" aria-labelledby="tieuDeCungLoai">
        <div class="section-heading">
            <div><p class="eyebrow">GỢI Ý CHO BẠN</p><h2 id="tieuDeCungLoai">Sản phẩm cùng loại</h2></div>
            <a class="online-detail-link" href="/#san-pham">Xem tất cả sản phẩm →</a>
        </div>
        <div class="product-grid">
            <c:forEach items="${sanPhamCungLoai}" var="sp">
                <article class="product-card">
                    <a class="product-picture" href="/san-pham/${sp.id}">
                        <span class="stock-label">Còn hàng</span>
                        <c:set var="anhGoiY" value="${sp.anh}"/>
                        <c:if test="${empty anhGoiY}"><c:set var="anhGoiY" value="/images/giay-mac-dinh.svg"/></c:if>
                        <c:if test="${not fn:startsWith(anhGoiY, '/') and not fn:startsWith(anhGoiY, 'http')}"><c:set var="anhGoiY" value="/${anhGoiY}"/></c:if>
                        <img src="<c:out value='${anhGoiY}'/>" alt="<c:out value='${sp.ten_san_pham}'/>" loading="lazy" width="640" height="420" onerror="this.onerror=null;this.src='/images/giay-mac-dinh.svg';">
                    </a>
                    <div class="product-content">
                        <p class="product-brand"><c:out value="${sp.ten_thuong_hieu}"/> <span>· <c:out value="${sp.ma_san_pham}"/></span></p>
                        <h3><a href="/san-pham/${sp.id}"><c:out value="${sp.ten_san_pham}"/></a></h3>
                        <p class="product-price"><small>Từ</small> <fmt:formatNumber value="${sp.gia_sau_giam}" groupingUsed="true"/> đ <c:if test="${sp.gia_sau_giam lt sp.gia_goc}"><del class="gia-goc"><fmt:formatNumber value="${sp.gia_goc}"/> đ</del></c:if></p>
                        <a class="online-detail-link" href="/san-pham/${sp.id}">Chọn size và mua hàng →</a>
                    </div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${not empty loiGoiY}"><p class="subtle"><c:out value="${loiGoiY}"/></p></c:if>
        <c:if test="${empty sanPhamCungLoai and empty loiGoiY}"><p class="subtle">Chưa có sản phẩm cùng loại còn hàng.</p></c:if>
    </section>
    </c:if>
</main>
<dialog id="huongDanSize" class="size-guide-dialog" aria-labelledby="tieuDeHuongDanSize">
    <div class="size-guide-heading">
        <h2 id="tieuDeHuongDanSize">Hướng dẫn chọn size</h2>
        <button type="button" id="dongHuongDanSize" aria-label="Đóng hướng dẫn chọn size">×</button>
    </div>
    <img src="/images/huong-dan-chon-size.png" alt="Bảng quy đổi size Việt Nam, size US, size UK và chiều dài bàn chân theo centimet">
</dialog>
<script src="/js/san-pham-online.js?v=20261019-single-product-image"></script></body></html>
