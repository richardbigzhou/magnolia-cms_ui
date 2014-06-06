info_magnolia_ui_workbench_tree_RowScroller = function() {

    var that = this;

    this.scrollRowIntoView = function(rowIndex) {
        setTimeout(function() {
            var tableBody = $('.mgnlTable' + that.getParentId()).find('.v-table-body');
            var rowViewport = tableBody.find('.v-table-table');
            var calculateRowHeight = function() {
                var rows = $('.v-table-row');
                if (rows.length > 0) {
                    return rows.first().height();
                } else {
                    var artificialRow = $(
                        '<div>', {
                            class: 'v-table-row '
                        });
                    rowViewport.append(artificialRow);
                    var height = artificialRow.height();
                    rowViewport.remove(artificialRow);
                    return height;
                }
            };

            var rowHeight = calculateRowHeight();
            tableBody.get(0).scrollTop = rowHeight * rowIndex - tableBody.height() / 2;
        }, 1);
    }
};