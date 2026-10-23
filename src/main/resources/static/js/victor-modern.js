/**
 * VictorSport Modern E-Commerce Interactive Script
 * Enhances UI with animations, countdown timer, hero slider, and interactive filters.
 */
window.VictorKhoiTaoModern = function () {
    var countdown = document.getElementById('flashSaleCountdown');
    if (countdown && !countdown.dataset.modernReady) {
        countdown.dataset.modernReady = 'true';
        var deadline = Number(countdown.dataset.han);
        function tick() {
            var seconds = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
            var values = [Math.floor(seconds / 3600), Math.floor(seconds / 60) % 60, seconds % 60];
            ['fsHours', 'fsMinutes', 'fsSeconds'].forEach(function (id, index) {
                countdown.querySelector('#' + id).textContent = deadline ? String(values[index]).padStart(2, '0') : '--';
            });
            return seconds > 0 && countdown.isConnected;
        }
        if (tick()) {
            var timer = setInterval(function () { if (!tick()) clearInterval(timer); }, 1000);
        }
        if (!deadline) countdown.setAttribute('aria-label', 'Chưa có đợt giảm giá đang diễn ra');
    }
    // Hero showcase
    function initHeroShowcase() {
        var slides = document.querySelectorAll(".shop-featured-slide");
        var dots = document.querySelectorAll(".hero-nav-dot");
        var thumbs = document.querySelectorAll(".hero-thumb-btn");
        if (slides.length <= 1) return;

        var currentIndex = 0;
        var autoTimer = null;
        var container = document.querySelector(".shop-featured");
        if (!container || container.dataset.modernReady) return;
        container.dataset.modernReady = 'true';

        function goToSlide(index) {
            slides[currentIndex].classList.remove("active");
            slides[currentIndex].setAttribute("aria-hidden", "true");
            slides[currentIndex].inert = true;
            if (dots[currentIndex]) dots[currentIndex].classList.remove("active");
            if (thumbs[currentIndex]) thumbs[currentIndex].classList.remove("active");

            currentIndex = (index + slides.length) % slides.length;

            slides[currentIndex].classList.add("active");
            slides[currentIndex].setAttribute("aria-hidden", "false");
            slides[currentIndex].inert = false;
            if (dots[currentIndex]) dots[currentIndex].classList.add("active");
            if (thumbs[currentIndex]) thumbs[currentIndex].classList.add("active");
        }

        // Attach dot events
        dots.forEach(function (dot, idx) {
            dot.addEventListener("click", function () {
                goToSlide(idx);
                resetAuto();
            });
        });

        // Attach thumbnail events
        thumbs.forEach(function (thumb, idx) {
            thumb.addEventListener("click", function () {
                goToSlide(idx);
                resetAuto();
            });
        });

        function startAuto() {
            if (autoTimer) clearInterval(autoTimer);
            autoTimer = setInterval(function () {
                if (!container.isConnected) { clearInterval(autoTimer); return; }
                if (!document.hidden && (!container || !container.matches(":hover"))) {
                    goToSlide(currentIndex + 1);
                }
            }, 4500);
        }

        function resetAuto() {
            startAuto();
        }

        startAuto();
    }
    initHeroShowcase();

    // 3. Filter Toggle (Advanced Filters)
    var btnToggleFilters = document.getElementById("btnToggleFilters");
    var advancedFilters = document.getElementById("advancedFilters");
    if (btnToggleFilters && advancedFilters && !btnToggleFilters.dataset.modernReady) {
        btnToggleFilters.dataset.modernReady = 'true';
        btnToggleFilters.addEventListener("click", function () {
            var isExpanded = advancedFilters.classList.toggle("is-open");
            btnToggleFilters.setAttribute("aria-expanded", isExpanded);
            btnToggleFilters.classList.toggle("active", isExpanded);
            var textSpan = btnToggleFilters.querySelector(".toggle-text");
            if (textSpan) {
                textSpan.textContent = isExpanded ? "Thu gọn bộ lọc" : "Lọc nâng cao";
            }
        });
    }

    // 4. Quick Search Clear Button
    var tuKhoaInput = document.getElementById("tuKhoa");
    var clearSearchBtn = document.getElementById("clearSearchBtn");
    if (tuKhoaInput && clearSearchBtn && !clearSearchBtn.dataset.modernReady) {
        clearSearchBtn.dataset.modernReady = 'true';
        function checkClear() {
            clearSearchBtn.hidden = !tuKhoaInput.value.trim();
        }
        tuKhoaInput.addEventListener("input", checkClear);
        clearSearchBtn.addEventListener("click", function () {
            tuKhoaInput.value = "";
            checkClear();
            tuKhoaInput.focus();
        });
        checkClear();
    }

    // 5. Back to Top Button
    var backToTopBtn = document.getElementById("backToTopBtn");
    if (backToTopBtn && !backToTopBtn.dataset.modernReady) {
        backToTopBtn.dataset.modernReady = 'true';
        window.addEventListener("scroll", function () {
            if (window.scrollY > 400) {
                backToTopBtn.classList.add("visible");
            } else {
                backToTopBtn.classList.remove("visible");
            }
        }, { passive: true });

        backToTopBtn.addEventListener("click", function () {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    }

    // 6. Header Scroll Effect (Glassmorphic blur enhancement on scroll)
    var storeHeader = document.querySelector(".store-header");
    if (storeHeader && !storeHeader.dataset.modernReady) {
        storeHeader.dataset.modernReady = 'true';
        window.addEventListener("scroll", function () {
            if (window.scrollY > 20) {
                storeHeader.classList.add("header-scrolled");
            } else {
                storeHeader.classList.remove("header-scrolled");
            }
        }, { passive: true });
    }

    // 7. Interactive Category Active Indication
    var categoryCards = document.querySelectorAll(".shop-category-card");
    categoryCards.forEach(function (card) {
        if (card.dataset.modernReady) return;
        card.dataset.modernReady = 'true';
        card.addEventListener("mouseenter", function () {
            this.style.transform = "translateY(-6px)";
        });
        card.addEventListener("mouseleave", function () {
            this.style.transform = "";
        });
    });
};
window.VictorKhoiTaoModern();
