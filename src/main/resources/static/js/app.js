// VICTOR SPORT - Core JavaScript Utility & Cart Management

const CartManager = {
    STORAGE_KEY: 'victor_sport_cart',

    getItems() {
        try {
            return JSON.parse(localStorage.getItem(this.STORAGE_KEY)) || [];
        } catch (e) {
            console.error('Error reading cart from localStorage', e);
            return [];
        }
    },

    saveItems(items) {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(items));
        this.updateBadge();
    },

    addItem(item) {
        // item = { idChiTiet, idSanPham, tenSanPham, mauSac, kichThuoc, formChan, gia, hinhAnh, soLuong, tonKho }
        const items = this.getItems();
        const existingIndex = items.findIndex(i => i.idChiTiet === item.idChiTiet);

        if (existingIndex > -1) {
            const newQty = items[existingIndex].soLuong + item.soLuong;
            if (item.tonKho && newQty > item.tonKho) {
                showToast('Thông báo', `Số lượng tồn kho chỉ còn ${item.tonKho} sản phẩm!`, 'warning');
                items[existingIndex].soLuong = item.tonKho;
            } else {
                items[existingIndex].soLuong = newQty;
            }
        } else {
            items.push(item);
        }

        this.saveItems(items);
        showToast('Thành công', `Đã thêm "${item.tenSanPham}" vào giỏ hàng!`, 'success');
    },

    updateQuantity(idChiTiet, delta) {
        const items = this.getItems();
        const item = items.find(i => i.idChiTiet === idChiTiet);
        if (item) {
            const nextQty = item.soLuong + delta;
            if (nextQty <= 0) {
                this.removeItem(idChiTiet);
                return;
            }
            if (item.tonKho && nextQty > item.tonKho) {
                showToast('Cảnh báo', `Tồn kho chỉ còn ${item.tonKho} đôi!`, 'warning');
                return;
            }
            item.soLuong = nextQty;
            this.saveItems(items);
        }
    },

    removeItem(idChiTiet) {
        let items = this.getItems();
        items = items.filter(i => i.idChiTiet !== idChiTiet);
        this.saveItems(items);
        showToast('Thông báo', 'Đã xóa sản phẩm khỏi giỏ hàng.', 'info');
    },

    clearCart() {
        localStorage.removeItem(this.STORAGE_KEY);
        this.updateBadge();
    },

    getTotalCount() {
        return this.getItems().reduce((sum, item) => sum + item.soLuong, 0);
    },

    getTotalPrice() {
        return this.getItems().reduce((sum, item) => sum + (item.gia * item.soLuong), 0);
    },

    updateBadge() {
        const badges = document.querySelectorAll('.cart-badge');
        const count = this.getTotalCount();
        badges.forEach(b => {
            b.textContent = count;
            b.style.display = count > 0 ? 'inline-block' : 'none';
        });
    }
};

// Format Currency
function formatVND(amount) {
    if (amount == null || isNaN(amount)) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// Show Toast
function showToast(title, message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toastId = 'toast-' + Date.now();
    const bgClass = type === 'success' ? 'bg-success text-white' :
                    type === 'warning' ? 'bg-warning text-dark' :
                    type === 'danger' ? 'bg-danger text-white' : 'bg-primary text-white';

    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center ${bgClass} border-0 shadow-lg mb-2" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="3000">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}:</strong> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastEl = document.getElementById(toastId);
    if (window.bootstrap && bootstrap.Toast) {
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
        toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    } else {
        setTimeout(() => toastEl.remove(), 3500);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    CartManager.updateBadge();
});
