/**
 * Shared "datatable list page" wiring — the search box, Excel/CSV/PDF/Print
 * export buttons, rows-per-page select, and "Showing X-Y of Z" + page-number
 * footer used by every list page (Student List, Employee List, and any
 * future one). Pair with static/css/list-table.css (the matching visual
 * chrome) and components/ui.html :: list-page-header (the title + stat tile
 * row above the table).
 *
 * DataTables' own auto-generated chrome (top length/search, bottom
 * info/paging) is always disabled here (layout: {...null}) — the search box,
 * export buttons, and footer/pagination are custom markup wired to the
 * DataTable's own client-side API (table.search()/.page()/.page.len()/
 * .buttons()), so every list page matches the same design pixel-for-pixel
 * instead of DataTables' stock Bootstrap look. The underlying data engine —
 * server-side ajax vs plain DOM rows, sorting, searching, paging — is
 * entirely up to `opts.dtOptions` and is untouched by this helper.
 *
 * @param {Object} opts
 * @param {string} opts.tableSelector          e.g. '#employee-list'
 * @param {string} opts.searchInputSelector    e.g. '#employeeSearchInput'
 * @param {string} opts.exportSlotSelector     e.g. '#empExportSlot'
 * @param {string} opts.lengthSelectSelector   e.g. '#empPageLength'
 * @param {string} opts.pageInfoSelector       e.g. '#empPageInfo'
 * @param {string} opts.paginationSelector     e.g. '#empPagination'
 * @param {string} opts.itemLabel              e.g. 'employees' — used in "Showing X-Y of Z <itemLabel>"
 * @param {string} opts.exportTitle            title/filename DataTables Buttons gives the Excel/CSV/PDF export
 * @param {number[]} opts.exportColumns        column indices included in the Excel/PDF export (exportOptions.columns)
 * @param {string} [opts.printHeaderText]      heading text for the Print button's printThis() header (defaults to exportTitle)
 * @param {Object} [opts.dtOptions]            extra/override DataTable() options (columns, ajax, serverSide, columnDefs, scrollX, ...) — page-specific, shallow-merged over the shared defaults. Do not pass `buttons` here — the shared export/print buttons own that key.
 * @returns {DataTable} the initialized DataTable instance
 */
function initListDataTable(opts) {
    const pageSizes = [5, 10, 25, 50, 100];
    const pageLabels = [5, 10, 25, 50, 100];

    const baseOptions = {
        layout: {
            topStart: null,
            topEnd: null,
            top1Start: null,
            bottomStart: null,
            bottomEnd: null
        },
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="bi bi-file-excel-fill"></i>',
                titleAttr: 'Excel',
                title: opts.exportTitle,
                className: 'list-export-btn',
                exportOptions: { columns: opts.exportColumns }
            },
            {
                extend: 'csvHtml5',
                text: '<i class="bi bi-filetype-csv"></i>',
                titleAttr: 'CSV',
                title: opts.exportTitle,
                className: 'list-export-btn',
                exportOptions: { columns: opts.exportColumns }
            },
            {
                extend: 'pdfHtml5',
                text: '<i class="bi bi-file-pdf-fill"></i>',
                titleAttr: 'PDF',
                title: opts.exportTitle,
                className: 'list-export-btn',
                exportOptions: { columns: opts.exportColumns }
            }
        ],
        lengthMenu: [pageSizes, pageLabels],
        pageLength: 25
    };

    // Shallow merge on purpose (not a deep/recursive merge) — page-specific
    // keys like columns/ajax/serverSide/columnDefs/scrollX are simply added
    // alongside the shared defaults above. A page that passes its own
    // `buttons` overrides the shared export buttons outright rather than
    // silently interleaving with them.
    const dtOptions = Object.assign({}, baseOptions, opts.dtOptions || {});
    const table = $(opts.tableSelector).DataTable(dtOptions);

    // Export/Print buttons — appended as plain code right after construction,
    // never inside initComplete: for a plain DOM-sourced (non-ajax) table,
    // DataTables finishes its whole first draw SYNCHRONOUSLY inside the
    // .DataTable({...}) call above, so initComplete would fire before an
    // outer "const table = ..." assignment has finished (a real bug found on
    // Employee List's first version of this page — a silent ReferenceError
    // swallowed by a try/catch, which is why only the export buttons were
    // missing while everything else worked). Placing this here instead
    // sidesteps the timing question entirely for both ajax and non-ajax
    // tables.
    try {
        $(opts.exportSlotSelector).append(table.buttons().container());
        $(opts.exportSlotSelector).append(
            $('<button type="button" class="list-export-btn" title="Print"><i class="bi bi-printer"></i></button>')
                .on('click', function () {
                    $(opts.tableSelector).printThis({
                        header: $('<h4>').text((opts.printHeaderText || opts.exportTitle) + ' — ' + new Date().toLocaleDateString()).prop('outerHTML'),
                        importCSS: true
                    });
                })
        );
    } catch (e) {
        console.error('Export/print buttons failed to initialize', e);
    }

    // Custom search pill (replaces DataTables' default top search box).
    let searchDebounce;
    $(opts.searchInputSelector).on('input', function () {
        const value = this.value;
        clearTimeout(searchDebounce);
        searchDebounce = setTimeout(function () { table.search(value).draw(); }, 300);
    });

    // Custom "rows per page" selector.
    $(opts.lengthSelectSelector).on('change', function () {
        table.page.len(parseInt(this.value, 10)).draw();
    });

    // Custom footer: "Showing X-Y of Z <itemLabel>" + numbered page buttons,
    // kept in sync with the table via the 'draw' event (fires after every
    // load — initial load, search, sort, page change, length change).
    const renderPagination = function () {
        const info = table.page.info();
        const shown = info.recordsDisplay === 0 ? 0 : info.start + 1;
        $(opts.pageInfoSelector).text(
            'Showing ' + shown + '–' + info.end + ' of ' + info.recordsDisplay.toLocaleString() + ' ' + opts.itemLabel
        );

        const $pg = $(opts.paginationSelector).empty();
        const cur = info.page, total = info.pages;

        const addBtn = function (label, page, btnOpts) {
            btnOpts = btnOpts || {};
            const $b = $('<button type="button" class="list-page-btn"></button>').html(label);
            if (btnOpts.active) { $b.addClass('active'); }
            if (btnOpts.disabled) {
                $b.addClass('disabled').attr('disabled', 'disabled');
            } else {
                $b.on('click', function () { table.page(page).draw('page'); });
            }
            $pg.append($b);
        };

        addBtn('<i class="bi bi-chevron-left"></i>', cur - 1, { disabled: cur <= 0 });

        let pages = [];
        if (total <= 7) {
            for (let i = 0; i < total; i++) { pages.push(i); }
        } else {
            pages.push(0);
            if (cur > 2) { pages.push('…'); }
            for (let j = Math.max(1, cur - 1); j <= Math.min(total - 2, cur + 1); j++) { pages.push(j); }
            if (cur < total - 3) { pages.push('…'); }
            if (total > 1) { pages.push(total - 1); }
        }
        pages.forEach(function (p) {
            if (p === '…') {
                $pg.append('<span class="list-page-ellipsis">…</span>');
                return;
            }
            addBtn(String(p + 1), p, { active: p === cur });
        });

        addBtn('<i class="bi bi-chevron-right"></i>', cur + 1, { disabled: cur >= total - 1 });
    };
    table.on('draw', renderPagination);
    renderPagination();

    return table;
}
