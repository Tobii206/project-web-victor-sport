<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<!DOCTYPE html>
<html lang="vi"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="referrer" content="no-referrer"><title>Thanh toán QR — VictorSport</title>
<link rel="icon" href="/logo/favicon-victorsport.svg">
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<style>
:root{color-scheme:light;--navy:#142d50;--ink:#172b46;--muted:#7b889d;--orange:#ff6b20;--line:#e7edf5}
*{box-sizing:border-box}body{margin:0;background:#f4f7fb;color:var(--ink);font:15px/1.6 'Be Vietnam Pro',Arial,sans-serif;-webkit-font-smoothing:antialiased}button,a{-webkit-tap-highlight-color:transparent}button{font:inherit}a{text-decoration:none;color:inherit}[hidden]{display:none!important}
.payment-nav{height:82px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;justify-content:space-between;padding:0 max(24px,calc((100vw - 1120px)/2));gap:16px}.payment-brand{font-weight:800;letter-spacing:-.5px;display:flex;align-items:center;gap:10px;font-size:20px}.payment-brand b{color:#e76825}.payment-logo{width:36px;height:39px;border-radius:10px;background:var(--navy);color:#ffb275;display:grid;place-items:center;font-style:italic;font-size:23px}.payment-nav-right{display:flex;align-items:center;gap:16px}.secure-label{display:flex;align-items:center;gap:7px;color:#7e8c9e;font-size:12px}.secure-label svg{width:15px;height:15px}.simulation-tag{border:1px solid #e2e8f0;background:#f7f9fc;color:#6c7b90;padding:4px 10px;border-radius:7px;font-size:11px;font-weight:600}
.payment-main{max-width:1040px;margin:42px auto 24px;padding:0 24px}.payment-heading{display:flex;align-items:center;justify-content:space-between;gap:20px;margin-bottom:25px}.payment-heading h1{font-size:27px;letter-spacing:-.8px;line-height:1.4;margin:3px 0 0}.payment-eyebrow{font-size:11px;color:#9aa5b4;letter-spacing:1.6px;font-weight:700;text-transform:uppercase}.payment-order-chip{font-size:12px;color:#7d8a9e;border:1px solid #e2e9f2;border-radius:10px;padding:9px 13px;background:#fff}.payment-order-chip strong{color:#243b5c;margin-left:6px}
.payment-shell{display:grid;grid-template-columns:380px minmax(0,1fr);background:#fff;border:1px solid #e0e7f0;border-radius:24px;overflow:hidden;box-shadow:0 18px 50px -25px #243b5c35}.payment-summary{background:linear-gradient(145deg,#19365b,#10253f);color:#fff;padding:31px 30px;position:relative}.summary-caption{color:#9eb4ce;font-size:12px;margin:0 0 22px}.merchant{display:flex;align-items:center;gap:12px;margin-bottom:27px}.merchant-icon{width:44px;height:44px;background:#ffffff12;border:1px solid #ffffff19;border-radius:13px;display:grid;place-items:center;color:#ffb577}.merchant-icon svg{width:22px;height:22px}.merchant strong{font-size:16px}.merchant small{display:block;color:#90a8c5;font-size:11px;margin-top:3px}.bank-panel{border:1px solid #ffffff1d;border-radius:16px;padding:17px 18px;background:#ffffff06}.bank-brand{display:flex;align-items:center;gap:10px;padding-bottom:17px;border-bottom:1px solid #ffffff13}.bank-mark{font-weight:800;font-style:italic;font-size:23px;letter-spacing:-1px;color:#8ed4ff}.bank-brand span:last-child{font-size:11px;line-height:1.5;color:#9db3cd}.account-label{margin:16px 0 3px;color:#99afca;font-size:11px}.account-number{font-size:21px;letter-spacing:1.1px;font-weight:600;white-space:nowrap}.account-holder{margin:9px 0 0;font-size:12px;font-weight:500;color:#dbe6f6;letter-spacing:.7px}.payment-meta{margin:23px 0 0}.payment-meta div{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:8px 0}.payment-meta dt{font-size:12px;color:#96acc7}.payment-meta dd{margin:0;font-size:12px;font-weight:500;text-align:right}.payment-total{border-top:1px dashed #ffffff2d;padding-top:21px;margin-top:22px}.payment-total span{display:block;font-size:12px;color:#9cb2cd}.payment-total strong{display:block;font-size:30px;letter-spacing:-1px;font-weight:700;color:#ffb16d;margin-top:5px}.payment-total strong small{font-size:18px}
.payment-content{padding:32px 34px;display:flex;align-items:center;flex-direction:column;text-align:center;min-height:565px}.state-icon{width:48px;height:48px;border:1px solid #ffdfca;background:#fff4eb;color:#e77c32;font-size:25px;border-radius:16px;display:grid;place-items:center;margin-bottom:15px}.payment-content h2{font-size:20px;letter-spacing:-.5px;line-height:1.4;margin:0 0 9px}.payment-status{font-size:12px;color:#8a97a8;min-height:21px;max-width:330px;line-height:1.7}.qr-frame{border:1px solid #e4eaf3;border-radius:20px;padding:13px;margin:22px auto 13px;width:232px;position:relative;background:#fff}.qr-frame:after{content:'';position:absolute;inset:-1px;border:2px solid #ffad7b;clip-path:polygon(0 0,15% 0,15% 2px,2px 2px,2px 15%,0 15%,0 0,100% 0,100% 15%,calc(100% - 2px) 15%,calc(100% - 2px) 2px,85% 2px,85% 0,100% 0,100% 100%,85% 100%,85% calc(100% - 2px),calc(100% - 2px) calc(100% - 2px),calc(100% - 2px) 85%,100% 85%,100% 100%,0 100%,0 85%,2px 85%,2px calc(100% - 2px),15% calc(100% - 2px),15% 100%,0 100%);border-radius:20px;pointer-events:none}.qr-demo-picture{display:block;width:204px;height:204px;max-width:100%;object-fit:contain}.qr-caption{font-size:11px;color:#9aa5b4;margin:0 0 20px}.qr-instructions{font-size:12px;line-height:1.7;color:#8794a7;margin:0 0 22px;max-width:280px}.payment-actions{width:100%;margin-top:auto;padding-top:18px}.payment-primary{display:flex;align-items:center;justify-content:center;gap:12px;width:100%;border:0;border-radius:12px;background:linear-gradient(100deg,#ff6923,#ff8b13);box-shadow:0 7px 20px #ff772428;color:#fff;font-size:14px;font-weight:700;min-height:48px;padding:13px 18px;cursor:pointer;transition:transform .15s,box-shadow .15s}.payment-primary:hover{transform:translateY(-1px);box-shadow:0 9px 24px #ff772440}.payment-primary:disabled{opacity:.6;cursor:wait}.payment-back{display:inline-flex;align-items:center;gap:6px;font-size:12px;color:#8794a8;margin-top:16px}.payment-back:hover{color:#203d62}.payment-tests{margin:20px 0 0;width:100%;border-top:1px solid var(--line);padding-top:15px;text-align:left}.payment-tests summary{font-size:11px;color:#98a4b4;cursor:pointer}.qr-demo-tests{display:flex;gap:10px;margin-top:12px}.qr-demo-tests button{font-size:11px;background:#f7f9fc;border:1px solid #e4eaf3;border-radius:8px;padding:8px 12px;color:#63758d;cursor:pointer}.payment-footer{text-align:center;color:#a4afbd;font-size:10px;margin:24px 0;letter-spacing:.2px}.payment-footer span{color:#8a99ad;font-weight:600}.phone-check{width:100%;border:1px solid var(--line);border-radius:14px;display:flex;justify-content:space-between;align-items:center;padding:15px 17px;margin:28px 0 0;gap:12px;font-size:12px;color:#8a98a8}.phone-check strong{color:#274466;font-size:13px}
#qrDemo[data-trang-thai='thanh_cong'] .state-icon{background:#e9f8f0;color:#249b6a;border-color:#cdeedf;box-shadow:0 0 0 10px #f6fcf9}#qrDemo[data-trang-thai='thanh_cong'] .payment-content{justify-content:center}#qrDemo[data-trang-thai='thanh_cong'] .state-icon{width:74px;height:74px;font-size:38px;margin:50px 0 26px;border-radius:50%}#qrDemo[data-trang-thai='thanh_cong'] .payment-actions{margin-top:36px;padding-top:0}#qrDemo:not([data-trang-thai='cho_thanh_toan']) .qr-stage{display:none}#qrDemo[data-trang-thai='that_bai'] .state-icon,#qrDemo[data-trang-thai='da_huy'] .state-icon{background:#fff0ee;color:#d56c60;border-color:#f7d9d4}
@media(max-width:760px){.payment-nav{height:66px;padding:0 20px}.payment-brand{font-size:17px}.secure-label{display:none}.payment-main{margin:24px auto;padding:0 16px}.payment-heading{gap:10px;margin-bottom:18px}.payment-heading h1{font-size:23px}.payment-order-chip{display:none}.payment-shell{grid-template-columns:1fr;border-radius:20px}.payment-summary{padding:24px}.summary-caption{margin-bottom:16px}.merchant{margin-bottom:20px}.payment-total strong{font-size:29px}.payment-content{min-height:0;padding:28px 24px}.payment-actions{margin-top:12px}#qrDemo[data-trang-thai='thanh_cong'] .state-icon{margin-top:12px}.payment-footer{font-size:10px}.payment-shell[data-dien-thoai='true'] .summary-caption{display:none}.payment-shell[data-dien-thoai='true'] .payment-meta{margin-top:15px}.payment-shell[data-dien-thoai='true'] .payment-total{margin-top:15px;padding-top:14px}.payment-shell[data-dien-thoai='true'] .bank-panel{padding:13px 16px}.payment-shell[data-dien-thoai='true'] .merchant{margin-bottom:16px}}
@media(prefers-reduced-motion:reduce){*{transition:none!important}}
@media(max-width:760px){#qrDemo[data-dien-thoai='true'][data-trang-thai='cho_thanh_toan'] .state-icon,#qrDemo[data-dien-thoai='true'][data-trang-thai='cho_thanh_toan'] #paymentStateTitle{display:none}#qrDemo[data-dien-thoai='true'] .phone-check{display:none}#qrDemo[data-dien-thoai='true'][data-trang-thai='cho_thanh_toan'] .payment-content{padding:18px 24px 22px}#qrDemo[data-dien-thoai='true'][data-trang-thai='cho_thanh_toan'] .payment-status:empty{display:none}#qrDemo[data-dien-thoai='true'][data-trang-thai='cho_thanh_toan'] .payment-actions{margin-top:0;padding-top:0}#qrDemo[data-dien-thoai='true'] .payment-tests{margin-top:14px;padding-top:12px}}

/* Cổng thanh toán dùng màu thương hiệu VictorSport. */
.bank-brand .bank-mark:last-child{font-size:24px;line-height:1.2;color:#8ed4ff}.gateway-logo{display:block;width:130px;height:36px;object-fit:contain}.payment-shell{border-radius:20px}.payment-primary{border-radius:10px}.payment-secondary{background:#fff;color:#284565;border:1px solid #d9e3ef;margin-top:10px;box-shadow:none}.payment-secondary:hover{background:#f6f8fc;box-shadow:none}.qr-provider-logo{display:block;width:125px;height:35px;object-fit:contain;margin:22px auto 0}.qr-frame{margin-top:15px}.qr-caption{font-size:10px;letter-spacing:1px}.payment-retry{margin-bottom:10px}.payment-heading h1{color:#142d50}.payment-content{min-height:555px}
@media(max-width:760px){.gateway-logo{width:112px;height:31px}.payment-shell{border-radius:16px}.payment-content{min-height:0}.qr-provider-logo{margin-top:18px}}
.payment-countdown{display:flex;align-items:center;justify-content:center;gap:8px;padding:7px 12px;border:1px solid #ffe1ca;border-radius:8px;background:#fff7ef;color:#94613c;font-size:11px;margin:0 0 18px}.payment-countdown strong{font-variant-numeric:tabular-nums;color:#d56824;font-size:15px;letter-spacing:1px}.payment-countdown.urgent{border-color:#f4c7be;background:#fff2ef}.payment-countdown.urgent strong{color:#c9503e}</style></head><body>
<nav class="payment-nav" aria-label="Thanh toán"><a class="payment-brand" href="/"><img class="gateway-logo" src="/images/vnpay.svg" alt="VNPAY" width="130" height="36"></a><div class="payment-nav-right"><span class="secure-label"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><rect x="5" y="10" width="14" height="11" rx="3"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>Cổng thanh toán</span><span class="simulation-tag">Mô phỏng</span></div></nav>
<main class="payment-main">
<header class="payment-heading"><div><span class="payment-eyebrow">THANH TOÁN TRỰC TUYẾN</span><h1>${trenDienThoai ? 'Xác nhận thanh toán' : 'Thanh toán QR'}</h1></div><span class="payment-order-chip">Đơn hàng<strong><c:out value="${giaoDich.ma_hoa_don}"/></strong></span></header>
<section class="payment-shell" id="qrDemo" data-id="${giaoDich.id}" data-csrf="<c:out value='${csrfQrDemo}'/>" data-trang-thai="<c:out value='${giaoDich.trang_thai}'/>" data-dien-thoai="${trenDienThoai == true}" data-ma="<c:out value='${maQuet}'/>" data-het-han="${giaoDich.het_han == true}" data-da-huy="${giaoDich.trang_thai_hien_tai == 6}" data-con-lai="${giaoDich.con_lai}">
<aside class="payment-summary" aria-label="Thông tin thanh toán">
<p class="summary-caption">THÔNG TIN THANH TOÁN</p>
<div class="merchant"><span class="merchant-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M4 10v10h16V10M3 10l2-7h14l2 7M3 10a3 3 0 0 0 5 0 3 3 0 0 0 4 0 3 3 0 0 0 4 0 3 3 0 0 0 5 0M9 20v-6h6v6"/></svg></span><div><strong>Cửa hàng VictorSport</strong><small>Hà Nội, Việt Nam</small></div></div>
<div class="bank-panel"><div class="bank-brand"><span class="bank-mark">NCB</span></div><p class="account-label">Số tài khoản</p><div class="account-number">0000 1234 5678</div><p class="account-holder">VICTOR SPORT</p></div>
<dl class="payment-meta"><div><dt>Mã đơn hàng</dt><dd><c:out value="${giaoDich.ma_hoa_don}"/></dd></div><div><dt>Nội dung thanh toán</dt><dd>Thanh toán <c:out value="${giaoDich.ma_hoa_don}"/></dd></div><div><dt>Phương thức</dt><dd>Chuyển khoản QR</dd></div></dl>
<div class="payment-total"><span>Tổng thanh toán</span><strong><fmt:formatNumber value="${giaoDich.so_tien}" maxFractionDigits="0"/> <small>đ</small></strong></div>
</aside>
<div class="payment-content">
<div class="payment-countdown" id="qrCountdown"><span>Mã QR hết hạn sau</span><strong id="qrCountdownValue">02:00</strong></div>
<div class="state-icon" id="paymentStateIcon" aria-hidden="true">↗</div><h2 id="paymentStateTitle">${trenDienThoai ? 'Xác nhận giao dịch' : 'Quét mã để thanh toán'}</h2>
<div id="qrDemoStatus" class="payment-status" role="status" aria-live="polite"></div>
<c:if test="${not trenDienThoai and giaoDich.trang_thai == 'cho_thanh_toan' and giaoDich.trang_thai_hien_tai != 6 and not giaoDich.het_han}"><div class="qr-stage"><img class="qr-provider-logo" src="/images/vnpay.svg" alt="VNPAY" width="125" height="35"><div class="qr-frame"><img class="qr-demo-picture" src="<c:out value='${anhQrDemo}'/>" width="204" height="204" alt="Mã QR thanh toán"/></div><p class="qr-caption">THANH TOÁN QUA MÃ QR</p><p class="qr-instructions" id="qrInstructions">Mở camera điện thoại và quét mã QR.</p></div></c:if>
<c:if test="${trenDienThoai and giaoDich.trang_thai == 'cho_thanh_toan' and giaoDich.trang_thai_hien_tai != 6}"><div class="phone-check qr-stage"><span>Đơn vị nhận</span><strong>VICTOR SPORT</strong></div></c:if>
<div class="payment-actions">
<c:if test="${not trenDienThoai}"><button type="button" class="payment-primary payment-retry" id="taiLaiQr" hidden>Tải lại mã QR <span aria-hidden="true">↻</span></button></c:if>
<c:if test="${trenDienThoai and giaoDich.trang_thai == 'cho_thanh_toan' and giaoDich.trang_thai_hien_tai != 6}"><button type="button" class="payment-primary" data-ket-qua="thanh_cong">Xác nhận thanh toán <span aria-hidden="true">→</span></button></c:if>
<c:if test="${not trenDienThoai}"><a class="payment-primary" id="xemDonDemo" href="${empty giaoDich.id_khach_hang ? '/don-vang-lai/' : '/don-hang/'}${giaoDich.id}">Xem đơn hàng <span aria-hidden="true">→</span></a></c:if>
<a class="payment-back" href="/">← Về trang chủ</a>
</div>
<c:if test="${giaoDich.trang_thai == 'cho_thanh_toan' and giaoDich.trang_thai_hien_tai != 6}"><details class="payment-tests" id="thuTinhHuong"><summary>Tùy chọn giao dịch</summary><div class="qr-demo-tests"><button type="button" data-ket-qua="that_bai">Giao dịch thất bại</button><button type="button" data-ket-qua="da_huy">Hủy thanh toán</button></div></details></c:if>
</div></section><footer class="payment-footer">Thanh toán trực tuyến cùng <span>VictorSport</span></footer>
</main>
<script>
(function () {
    var root=document.getElementById('qrDemo'), status=document.getElementById('qrDemoStatus');
    var timer, countdownTimer, busy=false, phone=root.dataset.dienThoai==='true';
    var remaining=root.dataset.conLai===''?120:Number(root.dataset.conLai);
    if(!Number.isFinite(remaining))remaining=120;
    var deadline=Date.now()+Math.max(0,remaining)*1000;
    var countdown=document.getElementById('qrCountdown'),clock=document.getElementById('qrCountdownValue');
    function hienThi(tt) {
        root.dataset.trangThai=tt;
        status.classList.toggle('done',tt==='thanh_cong');
        var title=document.getElementById('paymentStateTitle');
        if(title)title.textContent=tt==='thanh_cong'?'Thanh toán thành công':tt==='that_bai'?'Thanh toán chưa thành công':tt==='da_huy'?'Đã hủy thanh toán':(phone?'Xác nhận giao dịch':'Quét mã để thanh toán');
        var icon=document.getElementById('paymentStateIcon');
        if(icon)icon.textContent=tt==='thanh_cong'?'✓':tt==='that_bai'||tt==='da_huy'?'!':'↗';
        var retry=document.getElementById('taiLaiQr');
        var coTheTaiLai=!phone && root.dataset.daHuy!=='true' && (tt==='that_bai'||tt==='da_huy'||root.dataset.hetHan==='true' && tt==='cho_thanh_toan');
        if(retry)retry.hidden=!coTheTaiLai;
        var viewOrder=document.getElementById('xemDonDemo');if(viewOrder)viewOrder.classList.toggle('payment-secondary',coTheTaiLai);
        var instructions=document.getElementById('qrInstructions');if(instructions)instructions.hidden=tt!=='cho_thanh_toan';
        status.textContent=tt==='thanh_cong'?'Giao dịch đã được ghi nhận.':
            (tt==='that_bai'?'Giao dịch chưa thành công.':
            (tt==='da_huy'?'Giao dịch đã hủy.':
            (phone?'':'Đang chờ xác nhận từ điện thoại…')));
        if(countdown)countdown.hidden=tt!=='cho_thanh_toan';
        if(tt!=='cho_thanh_toan') {
            clearTimeout(timer);clearTimeout(countdownTimer);
            var extra=document.getElementById('thuTinhHuong');if(extra)extra.hidden=true;
            var pic=root.querySelector('.qr-demo-picture');if(pic)pic.hidden=true;
            root.querySelectorAll('[data-ket-qua]').forEach(function(b){b.hidden=true;});
        }
    }
    async function xuLy(tt) {
        if(busy || root.dataset.trangThai!=='cho_thanh_toan')return;
        busy=true;clearTimeout(timer);
        root.querySelectorAll('[data-ket-qua]').forEach(function(b){b.disabled=true;});
        status.textContent='Đang cập nhật thanh toán…';
        try {
            var body=new URLSearchParams({csrf:root.dataset.csrf,ketQua:tt});
            var url=phone?'/thanh-toan/qr-demo/quet/'+encodeURIComponent(root.dataset.ma)+'/xac-nhan':'/thanh-toan/qr-demo/'+root.dataset.id+'/ket-qua';
            var r=await fetch(url,{method:'POST',body:body,credentials:'same-origin',cache:'no-store'});
            var data=await r.json().catch(function(){return {};});
            if(!r.ok)throw new Error(data.error||'Chưa cập nhật được thanh toán.');
            hienThi(data.trangThai);
        } catch(e) {
            status.textContent=e.message||'Mất kết nối. Vui lòng thử lại.';
        } finally {
            busy=false;root.querySelectorAll('[data-ket-qua]').forEach(function(b){b.disabled=false;});
        }
    }
    async function theoDoi() {
        if(root.dataset.trangThai!=='cho_thanh_toan')return;
        try {
            var r=await fetch('/thanh-toan/qr-demo/'+root.dataset.id+'/trang-thai',{cache:'no-store',credentials:'same-origin'});
            if(!r.ok){status.textContent='Chưa tải được trạng thái. Vui lòng tải lại trang.';return;}
            var gd=await r.json();
            if(gd.daHuy){status.textContent='Đơn hàng đã hủy. Thanh toán đã dừng.';return;}
            if(gd.trangThai==='cho_thanh_toan' && typeof gd.conLai==='number')deadline=Date.now()+Math.max(0,gd.conLai)*1000;
            hienThi(gd.trangThai);
            if(gd.hetHan && gd.trangThai==='cho_thanh_toan') {
                root.dataset.hetHan='true';hienThi(gd.trangThai);
                status.textContent='Mã QR đã hết hạn. Vui lòng tải lại mã để tiếp tục.';
                var pic=root.querySelector('.qr-demo-picture');if(pic)pic.hidden=true;
                return;
            }
        }catch(e){status.textContent='Đang kết nối lại để kiểm tra thanh toán…';}
        if(root.dataset.trangThai==='cho_thanh_toan')timer=setTimeout(theoDoi,2000);
    }
    function demNguoc() {
        if(root.dataset.trangThai!=='cho_thanh_toan' || root.dataset.daHuy==='true')return;
        var seconds=Math.max(0,Math.ceil((deadline-Date.now())/1000));
        if(clock)clock.textContent=String(Math.floor(seconds/60)).padStart(2,'0')+':'+String(seconds%60).padStart(2,'0');
        if(countdown)countdown.classList.toggle('urgent',seconds<=10);
        if(seconds<=0) {
            root.dataset.hetHan='true';hienThi('that_bai');
            status.textContent='Mã QR đã hết hạn. Vui lòng tải lại mã để tiếp tục.';
            return;
        }
        countdownTimer=setTimeout(demNguoc,250);
    }
    var retryButton=document.getElementById('taiLaiQr');
    if(retryButton)retryButton.addEventListener('click',async function(){
        if(busy)return;
        busy=true;retryButton.disabled=true;status.textContent='Đang tạo mã QR mới…';
        try {
            var r=await fetch('/thanh-toan/qr-demo/'+root.dataset.id+'/tai-lai',{
                method:'POST',body:new URLSearchParams({csrf:root.dataset.csrf,maCu:root.dataset.ma}),credentials:'same-origin',cache:'no-store'
            });
            var data=await r.json().catch(function(){return {};});
            if(!r.ok)throw new Error(data.error||'Chưa tải lại được mã QR.');
            location.assign('/thanh-toan/qr-demo/'+root.dataset.id);
        }catch(e){status.textContent=e.message||'Mất kết nối. Vui lòng thử lại.';}
        finally{busy=false;retryButton.disabled=false;}
    });
    root.querySelectorAll('[data-ket-qua]').forEach(function(b){b.addEventListener('click',function(){xuLy(b.dataset.ketQua);});});
    if(root.dataset.daHuy==='true'){status.textContent='Đơn hàng đã hủy. Thanh toán đã dừng.';}
    else {
        hienThi(root.dataset.trangThai);demNguoc();
        if(!phone && root.dataset.trangThai==='cho_thanh_toan')timer=setTimeout(theoDoi,2000);
    }
    window.addEventListener('pagehide',function(){clearTimeout(timer);clearTimeout(countdownTimer);},{once:true});
}());
</script>
</body></html>
