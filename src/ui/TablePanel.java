package ui;

import logic.AlgorithmStep;
import model.Graph;
import model.Node;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/** The algorithm tracking table: node, shortest distance so far, previous node. */
public class TablePanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[] {
            "NODE", "<html><center>SHORTEST<br>DISTANCE</center></html>", "<html><center>PREVIOUS<br>NODE</center></html>"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final Set<Integer> changedRows = new HashSet<>();
    private final Set<Integer> visitedRows = new HashSet<>();
    private AlgorithmStep lastStep;

    public TablePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PANEL);

        table.setRowHeight(34);
        table.setFont(Theme.font(Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 202, 208));
        table.setRowSelectionAllowed(false);
        table.setFocusable(false);
        table.setBackground(Theme.PANEL);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setFont(Theme.font(Font.PLAIN, 12));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBackground(Theme.PANEL);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer cells = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                                     boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, false, false, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(Theme.INK);
                if (changedRows.contains(row)) setBackground(new Color(255, 236, 150));
                else if (visitedRows.contains(row)) setBackground(Theme.VISITED);
                else setBackground(Theme.PANEL);
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cells);

        // no scroll pane: only six rows, and this keeps the header always visible
        add(header, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    public void reset() { lastStep = null; }

    /** Show a snapshot. Changed rows are highlighted yellow, visited rows green. */
    public void update(AlgorithmStep step, Graph graph) {
        model.setRowCount(0);
        changedRows.clear();
        visitedRows.clear();
        int row = 0;
        for (Node n : graph.getNodes()) {
            int d = step.getDistance(n.getId());
            String prevId = step.getPrevious().get(n.getId());
            model.addRow(new Object[] {
                    n.getName(),
                    d == AlgorithmStep.INFINITY ? "\u221e" : String.valueOf(d),
                    prevId == null ? "-" : graph.getNode(prevId).getName()});
            if (step.getVisited().contains(n.getId())) visitedRows.add(row);
            if (lastStep != null && lastStep.getDistance(n.getId()) != d) changedRows.add(row);
            row++;
        }
        lastStep = step;
        table.repaint();
    }
}
