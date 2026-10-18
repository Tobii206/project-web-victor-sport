<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="/css/chuong-thong-bao.css?v=1">
<div class="vs-bell" data-kenh="${param.kenhChuong}">
    <button class="vs-bell-button" type="button" title="Thông báo đơn hàng" aria-label="Thông báo đơn hàng" aria-expanded="false">
        <svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>
        <span class="vs-bell-count" hidden></span>
    </button>
    <section class="vs-bell-panel" aria-label="Thông báo đơn hàng" hidden>
        <div class="vs-bell-heading"><h2>Thông báo</h2><button type="button" class="vs-bell-read-all" disabled>Đọc tất cả</button></div>
        <p class="vs-bell-status" role="status">Đang tải thông báo…</p>
        <div class="vs-bell-list"></div>
        <button type="button" class="vs-bell-retry" hidden>Thử lại</button>
        <p class="vs-bell-footer">20 thông báo gần nhất · Tự cập nhật</p>
    </section>
</div>
<script src="/js/chuong-thong-bao.js?v=3" defer></script>
