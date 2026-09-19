/*
 * SmsPrintUtils - shared, CDN-free print / PDF-export helpers.
 *
 * Replaces the old per-template printDiv() pattern (window.open() + a
 * hardcoded, version-pinned CDN <link> for Bootstrap) with two alternatives
 * that stay visually identical to the live page and never depend on an
 * external network call at print time:
 *
 *   - printElement(): uses printThis (jQuery plugin, already local to this
 *     app) to clone the target element into a hidden iframe and print it.
 *     printThis pulls in whatever local stylesheets the CURRENT page is
 *     already using, so the printed output automatically tracks this app's
 *     real, current Bootstrap/CSS version - there's no separate copy to
 *     fall out of date the way the old hardcoded CDN link (pinned to an
 *     older Bootstrap version than this app actually ships) did.
 *
 *   - downloadElementAsPdf(): uses html2canvas + jsPDF (both already
 *     downloaded locally under /js/) to rasterize the element exactly as
 *     rendered on screen and save it as a real .pdf file. Because it's a
 *     pixel capture of the live DOM, the design can never drift from what
 *     the user actually sees - the tradeoff is the PDF's text isn't
 *     selectable/searchable (it's an image), which is fine for a receipt
 *     meant to be looked at or printed, not edited. This also currently
 *     only fits the capture onto a single PDF page - fine for a short
 *     receipt, but would need paging logic added if ever reused for
 *     multi-page content.
 *
 * Requires (load in this order, before this file):
 *   jQuery, printThis.js  - for printElement()
 *   html2canvas.min.js, jspdf.umd.min.js - for downloadElementAsPdf()
 * All four are already served locally by this app; nothing here reaches
 * out to any CDN.
 */
(function (window, $) {
    'use strict';

    function withCdnStylesheetsDisabled(fn) {
        // printThis() clones every <link rel="stylesheet"> currently on the
        // page as-is - it has no include/exclude option of its own. This
        // app serves almost all of its CSS locally, but keeps one CDN
        // fallback link (Bootstrap Icons) alongside the local copy in
        // base.html. Temporarily flip any CDN stylesheet link's rel
        // attribute off before printThis's synchronous DOM read, then
        // restore it immediately after, so the printed/exported output only
        // ever pulls from this app's own server - never an external CDN.
        var $cdnLinks = $('link[rel="stylesheet"][href*="cdn."]');
        $cdnLinks.attr('rel', 'stylesheet-disabled-for-print');
        try {
            fn();
        } finally {
            $cdnLinks.attr('rel', 'stylesheet');
        }
    }

    function printElement(divId, title) {
        var $el = $('#' + divId);
        if ($el.length === 0) {
            console.error('SmsPrintUtils.printElement: no element with id="' + divId + '"');
            return;
        }
        if (typeof $el.printThis !== 'function') {
            console.error('SmsPrintUtils.printElement: printThis.js is not loaded on this page');
            return;
        }
        withCdnStylesheetsDisabled(function () {
            $el.printThis({
                importCSS: true,     // pull in this app's own local stylesheets (already on the page)
                importStyle: true,   // and any inline <style> blocks
                copyTagStyles: true, // carry CSS custom properties set on :root/body
                pageTitle: title || document.title,
                printDelay: 500
            });
        });
    }

    function downloadElementAsPdf(divId, filename) {
        var el = document.getElementById(divId);
        if (!el) {
            console.error('SmsPrintUtils.downloadElementAsPdf: no element with id="' + divId + '"');
            return;
        }
        if (typeof window.html2canvas === 'undefined' || typeof window.jspdf === 'undefined') {
            console.error('SmsPrintUtils.downloadElementAsPdf: html2canvas/jsPDF are not loaded on this page');
            alert('PDF export is not available right now. Please try Print instead.');
            return;
        }
        window.html2canvas(el, { scale: 2, useCORS: true }).then(function (canvas) {
            var imgData = canvas.toDataURL('image/png');
            var jsPDF = window.jspdf.jsPDF;
            var pdf = new jsPDF('p', 'pt', 'a4');
            var pageWidth = pdf.internal.pageSize.getWidth();
            var margin = 20;
            var imgWidth = pageWidth - (margin * 2);
            var imgHeight = (canvas.height * imgWidth) / canvas.width;
            pdf.addImage(imgData, 'PNG', margin, margin, imgWidth, imgHeight);
            pdf.save(filename || 'document.pdf');
        }).catch(function (err) {
            console.error('SmsPrintUtils.downloadElementAsPdf failed:', err);
            alert('Could not generate the PDF. Please try again.');
        });
    }

    window.SmsPrintUtils = {
        printElement: printElement,
        downloadElementAsPdf: downloadElementAsPdf
    };

})(window, jQuery);
