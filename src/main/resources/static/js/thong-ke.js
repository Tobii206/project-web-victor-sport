(function () {
    'use strict';
    var colors = ['#f59e0b', '#6366f1', '#3b82f6', '#06b6d4', '#16a085', '#ef6464'];
    var money = new Intl.NumberFormat('vi-VN', {maximumFractionDigits: 0});
    var compact = new Intl.NumberFormat('vi-VN', {notation: 'compact', maximumFractionDigits: 1});
    var rendered = new WeakMap(), closing = new WeakSet();
    function reducedMotion() {
        return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    }
    function motion(element, frames, duration, delay) {
        if (!element.animate || reducedMotion()) return Promise.resolve();
        return element.animate(frames, {duration: duration, delay: delay || 0, fill: 'backwards', easing: 'cubic-bezier(.2,.8,.2,1)'}).finished.catch(function () {});
    }
    function chartMotion(element, frames, duration, delay) {
        if (element.getAnimations) element.getAnimations().forEach(function (animation) { animation.cancel(); });
        motion(element, frames, duration, delay);
    }
    function revealChart(chart) {
        chart.querySelectorAll('polyline').forEach(function (path) {
            var length = path.getTotalLength();
            if (length > 0) chartMotion(path, [{strokeDasharray: length + ' ' + length, strokeDashoffset: length}, {strokeDasharray: length + ' ' + length, strokeDashoffset: 0}], 900, 100);
        });
        chart.querySelectorAll('polygon').forEach(function (area) {
            chartMotion(area, [{opacity: 0}, {opacity: 1}], 650, 180);
        });
        var dots = chart.querySelectorAll('.stats-line-point');
        dots.forEach(function (dot, index) {
            chartMotion(dot, [{opacity: 0}, {opacity: 1}], 180, 100 + index / Math.max(1, dots.length - 1) * 800);
        });
        var elapsed = 0;
        chart.querySelectorAll('.stats-donut-arc').forEach(function (arc) {
            var target = arc.getAttribute('stroke-dasharray'), circumference = 2 * Math.PI * 78;
            var duration = Number.parseFloat(target) / circumference * 850;
            chartMotion(arc, [{strokeDasharray: '0 ' + circumference}, {strokeDasharray: target}], duration, 100 + elapsed);
            elapsed += duration;
        });
    }
    function animateDialogCharts(dialog) {
        dialog.querySelectorAll('.stats-line-canvas svg,.stats-pie-canvas svg').forEach(revealChart);
        dialog.querySelectorAll('.revenue-track > span,.stats-progress > span,.stats-sold-bar > span').forEach(function (bar, index) {
            var vertical = bar.parentElement.classList.contains('revenue-track');
            bar.style.transformOrigin = vertical ? 'center bottom' : 'left center';
            chartMotion(bar, [{transform: vertical ? 'scaleY(0)' : 'scaleX(0)'}, {transform: vertical ? 'scaleY(1)' : 'scaleX(1)'}], 650, 100 + Math.min(index * 28, 400));
        });
        dialog.querySelectorAll('tbody tr').forEach(function (row, index) {
            chartMotion(row, [{opacity: 0, transform: 'translateY(6px)'}, {opacity: 1, transform: 'translateY(0)'}], 280, Math.min(index * 35, 350));
        });
    }
    function syncLaunchers() {
        document.querySelectorAll('[data-stats-open]').forEach(function (button) {
            var dialog = document.getElementById(button.dataset.statsOpen);
            button.setAttribute('aria-expanded', !!(dialog && dialog.open));
        });
    }
    function pressFeedback(button, event) {
        if (reducedMotion()) return;
        button.querySelectorAll('.stats-click-wave').forEach(function (wave) { wave.remove(); });
        var rect = button.getBoundingClientRect(), size = Math.max(rect.width, rect.height) * 2;
        var wave = document.createElement('span');
        wave.className = 'stats-click-wave';wave.setAttribute('aria-hidden', 'true');
        wave.style.width = wave.style.height = size + 'px';
        wave.style.left = ((event.detail === 0 ? rect.width / 2 : event.clientX - rect.left) - size / 2) + 'px';
        wave.style.top = ((event.detail === 0 ? rect.height / 2 : event.clientY - rect.top) - size / 2) + 'px';
        button.appendChild(wave);
        motion(wave, [{transform: 'scale(0)', opacity: .5}, {transform: 'scale(1)', opacity: 0}], 420).then(function () { wave.remove(); });
    }
    function closeDialog(dialog) {
        if (!dialog || !dialog.open || closing.has(dialog)) return;
        closing.add(dialog);
        dialog.querySelectorAll('.stats-dialog-body *').forEach(function (element) {
            if (element.getAnimations) element.getAnimations().forEach(function (animation) { animation.cancel(); });
        });
        motion(dialog, [{opacity: 1, transform: 'translateY(0) scale(1)'}, {opacity: 0, transform: 'translateY(8px) scale(.99)'}], 140).then(function () {
            if (dialog.open) dialog.close();
            closing.delete(dialog);
        });
    }
    function svg(tag, attrs, text) {
        var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
        Object.keys(attrs || {}).forEach(function (key) { el.setAttribute(key, attrs[key]); });
        if (text !== undefined) el.textContent = text;
        return el;
    }
    function pie(box) {
        var items = Array.from(box.querySelectorAll('li[data-value]'));
        var signature = box.dataset.format + items.map(function (item) { return item.dataset.label + ':' + item.dataset.value; }).join('|');
        if (rendered.get(box) === signature) return;
        var total = items.reduce(function (sum, item) { return sum + Math.max(0, Number(item.dataset.value) || 0); }, 0);
        var canvas = box.querySelector('.stats-pie-canvas'), chart = svg('svg', {viewBox: '0 0 220 220', 'aria-hidden': 'true'});
        chart.appendChild(svg('circle', {cx: 110, cy: 110, r: 78, fill: 'none', stroke: '#edf1f7', 'stroke-width': 28}));
        var offset = 0, circumference = 2 * Math.PI * 78;
        items.forEach(function (item, index) {
            var value = Math.max(0, Number(item.dataset.value) || 0), fraction = total ? value / total : 0;
            item.style.setProperty('--slice-color', colors[index % colors.length]);
            if (!fraction) return;
            var arc = svg('circle', {class: 'stats-donut-arc', cx: 110, cy: 110, r: 78, fill: 'none', stroke: colors[index % colors.length], 'stroke-width': 28,
                'stroke-dasharray': (fraction * circumference) + ' ' + circumference,
                'stroke-dashoffset': -offset * circumference, transform: 'rotate(-90 110 110)', tabindex: '0'});
            var label = item.dataset.label + ': ' + money.format(value) + (box.dataset.format === 'money' ? ' đ' : ' đơn') + ' (' + (fraction * 100).toFixed(1) + '%)';
            arc.appendChild(svg('title', {}, label));arc.setAttribute('aria-label', label);chart.appendChild(arc);offset += fraction;
        });
        chart.appendChild(svg('text', {x: 110, y: 108, 'text-anchor': 'middle', class: 'stats-donut-total'}, compact.format(total)));
        chart.appendChild(svg('text', {x: 110, y: 132, 'text-anchor': 'middle', class: 'stats-donut-caption'}, total ? (box.dataset.format === 'money' ? 'đồng' : 'đơn hàng') : 'Chưa có dữ liệu'));
        canvas.setAttribute('aria-label', canvas.getAttribute('aria-label').split(' — ')[0] + ' — ' + (total ? items.map(function (i) { return i.dataset.label + ': ' + i.dataset.value; }).join(', ') : 'Chưa có dữ liệu'));
        canvas.replaceChildren(chart);
        rendered.set(box, signature);revealChart(chart);
    }
    function line(panel) {
        var data = Array.from(panel.querySelectorAll('.stats-line-data span')), kind = panel.dataset.type || 'revenue';
        var signature = kind + data.map(function (d) { return d.dataset.date + ':' + d.dataset[kind]; }).join('|');
        if (rendered.get(panel) === signature) return;
        var canvas = panel.querySelector('.stats-line-canvas');
        if (!data.length) { canvas.textContent = 'Chưa có số liệu trong kỳ.';return; }
        var values = data.map(function (d) { return Math.max(0, Number(d.dataset[kind]) || 0); });
        var high = Math.max.apply(null, values), max = kind === 'orders' ? Math.max(1, high) : Math.max(1000, high);
        var chart = svg('svg', {viewBox: '0 0 880 280', 'aria-hidden': 'true'}), left = 72, top = 22, width = 776, height = 208;
        for (var i = 0; i <= 4; i++) {
            var y = top + height * i / 4, value = max * (1 - i / 4);
            chart.appendChild(svg('line', {x1: left, y1: y, x2: left + width, y2: y, stroke: '#e6edf5', 'stroke-dasharray': '4 5'}));
            chart.appendChild(svg('text', {x: left - 12, y: y + 4, 'text-anchor': 'end', class: 'stats-axis-label'}, kind === 'orders' ? Number(value.toFixed(1)).toString() : compact.format(value)));
        }
        var points = values.map(function (value, index) {
            return [data.length === 1 ? left + width / 2 : left + index * width / (data.length - 1), top + height * (1 - value / max)];
        });
        chart.appendChild(svg('polygon', {points: [[points[0][0], top + height]].concat(points, [[points[points.length - 1][0], top + height]]).map(function (p) { return p.join(','); }).join(' '), fill: '#ff761014'}));
        chart.appendChild(svg('polyline', {points: points.map(function (p) { return p.join(','); }).join(' '), fill: 'none', stroke: '#ff6c23', 'stroke-width': 3, 'stroke-linejoin': 'round'}));
        points.forEach(function (p, index) {
            var dot = svg('circle', {class: 'stats-line-point', cx: p[0], cy: p[1], r: data.length > 90 ? 2 : 4, fill: '#ff6c23', tabindex: '0'});
            var label = data[index].dataset.date + ': ' + money.format(values[index]) + (kind === 'orders' ? ' đơn' : ' đ');
            dot.appendChild(svg('title', {}, label));dot.setAttribute('aria-label', label);chart.appendChild(dot);
            if (index === 0 || index === data.length - 1 || index % Math.ceil(data.length / 7) === 0) chart.appendChild(svg('text', {x: p[0], y: 257, 'text-anchor': 'middle', class: 'stats-axis-label'}, data[index].dataset.label));
        });
        canvas.setAttribute('aria-label', 'Biểu đồ đường ' + (kind === 'orders' ? 'số đơn hoàn tất' : 'doanh thu') + ' từ ' + data[0].dataset.date + ' đến ' + data[data.length - 1].dataset.date);
        canvas.replaceChildren(chart);
        rendered.set(panel, signature);revealChart(chart);
        panel.querySelectorAll('[data-line-type]').forEach(function (button) { button.setAttribute('aria-pressed', button.dataset.lineType === kind); });
    }
    window.VictorKhoiTaoThongKe = function () {
        syncLaunchers();
        document.documentElement.classList.toggle('stats-modal-open', !!document.querySelector('.stats-dialog[open]'));
        document.querySelectorAll('.stats-pie').forEach(function (box) {
            var dialog = box.closest('.stats-dialog');
            if (!dialog || dialog.open) pie(box);
        });
        document.querySelectorAll('.stats-line-panel').forEach(function (panel) {
            var dialog = panel.closest('.stats-dialog');
            if (!dialog || dialog.open) line(panel);
        });
    };
    document.addEventListener('click', function (event) {
        var opener = event.target.closest('[data-stats-open]');
        if (opener) {
            if (event.detail === 0) pressFeedback(opener, event);
            var dialog = document.getElementById(opener.dataset.statsOpen);
            if (dialog && !dialog.open) {
                dialog.showModal();
                document.documentElement.classList.add('stats-modal-open');
                window.VictorKhoiTaoThongKe();
                animateDialogCharts(dialog);
                motion(dialog, [{opacity: 0, transform: 'translateY(18px) scale(.97)'}, {opacity: 1, transform: 'translateY(0) scale(1)'}], 280);
            }
            return;
        }
        var closer = event.target.closest('[data-stats-close]');
        if (closer) { closeDialog(closer.closest('dialog')); return; }
        var button = event.target.closest('[data-line-type]');
        if (!button) return;
        var panel = button.closest('.stats-line-panel');
        if (!panel) return;
        panel.dataset.type = button.dataset.lineType;line(panel);
    });
    // Capture also handles dialogs replaced by live updates or in-place navigation.
    document.addEventListener('close', function (event) {
        if (event.target.matches('.stats-dialog')) {
            syncLaunchers();
            document.documentElement.classList.toggle('stats-modal-open', !!document.querySelector('.stats-dialog[open]'));
        }
    }, true);
    document.addEventListener('cancel', function (event) {
        if (event.target.matches('.stats-dialog')) {
            event.preventDefault();closeDialog(event.target);
        }
    }, true);
    var backdropDialog = null;
    function outsideDialog(event) {
        if (!event.target.matches('.stats-dialog')) return false;
        var rect = event.target.getBoundingClientRect();
        return event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom;
    }
    document.addEventListener('pointerdown', function (event) {
        var button = event.target.closest('[data-stats-open]');
        if (button && event.button === 0) pressFeedback(button, event);
        backdropDialog = outsideDialog(event) ? event.target : null;
    });
    document.addEventListener('pointerup', function (event) {
        if (backdropDialog === event.target && outsideDialog(event)) closeDialog(event.target);
        backdropDialog = null;
    });
    document.addEventListener('vs:du-lieu-da-cap-nhat', window.VictorKhoiTaoThongKe);
    window.VictorKhoiTaoThongKe();
})();
