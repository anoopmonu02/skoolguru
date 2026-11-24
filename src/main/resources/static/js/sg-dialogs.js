/**
 * sg-dialogs.js — shared confirm / message dialogs for SkoolGuru screens.
 *
 *   SG.confirm({title, message, tone, confirmText, cancelText, items, note,
 *               acknowledge, icon, extraButton})  -> Promise<boolean | string>
 *   SG.alert({title, message, tone, okText, items, note, icon}) -> Promise<void>
 *   SG.toast(type, message, title)  -> thin wrapper over toastr
 *
 * tone: 'info' | 'success' | 'warning' | 'danger'  (default 'info').
 * Every piece of text goes in through textContent, never innerHTML, so values
 * that come from the server (student names, error text) are always safe.
 *
 * confirm() resolves true on confirm, false on cancel / Esc / backdrop. When an
 * `extraButton` {text, value} is supplied (e.g. "OK & create new group") the
 * promise resolves with that button's `value` instead.
 * `acknowledge` (string) adds a checkbox that must be ticked before the
 * confirm button is enabled.
 *
 * Loaded last (after Bootstrap + jQuery) from base.html. Adds one dialog node
 * to <body> on first use; touches nothing else on the page.
 */
(function (global) {
    'use strict';

    var ICONS = { info: 'bi-info-circle-fill', success: 'bi-check-circle-fill', warning: 'bi-exclamation-triangle-fill', danger: 'bi-trash3-fill' };
    var DEFAULT_ICON_BY_TONE = { info: 'bi-info-circle-fill', success: 'bi-check-circle-fill', warning: 'bi-exclamation-triangle-fill', danger: 'bi-exclamation-octagon-fill' };

    function el(tag, cls, text) {
        var n = document.createElement(tag);
        if (cls) { n.className = cls; }
        if (text != null) { n.textContent = text; }
        return n;
    }

    function build(opts, mode) {
        var tone = ICONS[opts.tone] ? opts.tone : 'info';
        var root = el('div', 'modal fade sg-dialog sg-tone-' + tone);
        root.tabIndex = -1;
        root.setAttribute('aria-hidden', 'true');
        root.setAttribute('role', 'dialog');
        root.setAttribute('aria-modal', 'true');

        var dlg = el('div', 'modal-dialog modal-dialog-centered');
        dlg.style.maxWidth = (opts.width || 440) + 'px';
        var content = el('div', 'modal-content');
        var body = el('div', 'sg-dialog-body');

        var icon = el('div', 'sg-dialog-icon');
        icon.appendChild(el('i', 'bi ' + (opts.icon || DEFAULT_ICON_BY_TONE[tone])));
        body.appendChild(icon);

        var titleId = 'sgDialogTitle' + Date.now();
        var title = el('h5', 'sg-dialog-title', opts.title || (mode === 'alert' ? 'Notice' : 'Are you sure?'));
        title.id = titleId;
        root.setAttribute('aria-labelledby', titleId);
        body.appendChild(title);

        if (opts.message) { body.appendChild(el('p', 'sg-dialog-message', opts.message)); }

        if (opts.items && opts.items.length) {
            var ul = el('ul', 'sg-dialog-list');
            opts.items.forEach(function (it) {
                var li = el('li');
                if (it && typeof it === 'object') {
                    li.appendChild(el('span', null, it.label));
                    if (it.value != null) { li.appendChild(el('span', null, it.value)); }
                } else {
                    li.appendChild(el('span', null, it));
                }
                ul.appendChild(li);
            });
            body.appendChild(ul);
        }
        if (opts.note) { body.appendChild(el('div', 'sg-dialog-note', opts.note)); }

        var ackBox = null;
        if (mode === 'confirm' && opts.acknowledge) {
            var lab = el('label', 'sg-dialog-ack');
            ackBox = el('input');
            ackBox.type = 'checkbox';
            lab.appendChild(ackBox);
            lab.appendChild(el('span', null, opts.acknowledge));
            body.appendChild(lab);
        }

        var footer = el('div', 'modal-footer sg-dialog-footer');
        var cancelBtn = null, extraBtn = null, okBtn;
        if (mode === 'confirm') {
            cancelBtn = el('button', 'btn sg-btn-ghost', opts.cancelText || 'Cancel');
            cancelBtn.type = 'button';
            footer.appendChild(cancelBtn);
            if (opts.extraButton) {
                extraBtn = el('button', 'btn sg-btn-ghost', opts.extraButton.text);
                extraBtn.type = 'button';
                footer.appendChild(extraBtn);
            }
        }
        okBtn = el('button', 'btn sg-btn-tone', opts.confirmText || opts.okText || (mode === 'alert' ? 'OK' : 'Confirm'));
        okBtn.type = 'button';
        footer.appendChild(okBtn);
        if (ackBox) {
            okBtn.disabled = true;
            if (extraBtn) { extraBtn.disabled = true; }
            ackBox.addEventListener('change', function () {
                okBtn.disabled = !ackBox.checked;
                if (extraBtn) { extraBtn.disabled = !ackBox.checked; }
            });
        }

        content.appendChild(body);
        content.appendChild(footer);
        dlg.appendChild(content);
        root.appendChild(dlg);
        return { root: root, ok: okBtn, cancel: cancelBtn, extra: extraBtn };
    }

    function open(opts, mode) {
        opts = opts || {};
        return new Promise(function (resolve) {
            if (typeof bootstrap === 'undefined') {
                // Bootstrap missing: degrade to the browser's own dialogs rather than breaking the action.
                var text = (opts.title ? opts.title + '\n\n' : '') + (opts.message || '');
                if (mode === 'alert') { global.alert(text); resolve(); } else { resolve(global.confirm(text)); }
                return;
            }
            var parts = build(opts, mode);
            document.body.appendChild(parts.root);
            var modal = new bootstrap.Modal(parts.root, { backdrop: mode === 'alert' ? true : 'static', keyboard: true });
            var result = mode === 'alert' ? undefined : false;
            var settled = false;

            function finish(value) { result = value; modal.hide(); }
            parts.ok.addEventListener('click', function () { finish(mode === 'alert' ? undefined : true); });
            if (parts.cancel) { parts.cancel.addEventListener('click', function () { finish(false); }); }
            if (parts.extra) { parts.extra.addEventListener('click', function () { finish(opts.extraButton.value); }); }

            parts.root.addEventListener('shown.bs.modal', function () {
                // A destructive action starts focused on Cancel so Enter can't delete by accident.
                var first = (opts.tone === 'danger' && parts.cancel) ? parts.cancel : parts.ok;
                if (!first.disabled) { first.focus(); }
            });
            parts.root.addEventListener('hidden.bs.modal', function () {
                if (settled) { return; }
                settled = true;
                modal.dispose();
                parts.root.remove();
                resolve(result);
            });
            modal.show();
        });
    }

    var SG = global.SG || {};
    SG.confirm = function (opts) { return open(opts, 'confirm'); };
    SG.alert = function (opts) { return open(opts, 'alert'); };
    SG.toast = function (type, message, title) {
        if (typeof global.showMsg === 'function') { global.showMsg(type, message, title || ''); }
        else if (global.toastr && global.toastr[type]) { global.toastr[type](message, title || ''); }
    };
    global.SG = SG;
})(window);
