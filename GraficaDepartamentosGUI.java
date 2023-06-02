package Documentos;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraficaDepartamentosGUI extends JFrame {
    private List<String> meses;
    private List<String> departamentos;
    private int[][] ventas;
    private String rutaArchivo;

    private JTable tabla;
    private JComboBox<String> comboBoxMeses;
    private JButton btnGraficar;

    public GraficaDepartamentosGUI() {
        super("Gráfica de Departamentos");
        meses = new ArrayList<>();
        departamentos = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tabla de datos
        tabla = new JTable();
        JScrollPane scrollPane = new JScrollPane(tabla);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de control
        JPanel panelControl = new JPanel();

        JLabel lblMes = new JLabel("Seleccionar mes:");
        panelControl.add(lblMes);

        comboBoxMeses = new JComboBox<>();
        panelControl.add(comboBoxMeses);

        btnGraficar = new JButton("Generar gráficas");
        panelControl.add(btnGraficar);

        panel.add(panelControl, BorderLayout.SOUTH);

        // Agregar panel al frame principal
        add(panel);

        // Eventos de botones
        btnGraficar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mesSeleccionado = (String) comboBoxMeses.getSelectedItem();
                if (mesSeleccionado != null) {
                    mostrarGrafica(mesSeleccionado);
                }
            }
        });
    }

    private void cargarDatosTabla() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Permitir la edición de todas las celdas excepto la primera columna
                return column != 0;
            }
        };

        // Columnas
        model.addColumn("Departamento");
        for (String mes : meses) {
            model.addColumn(mes);
        }

        // Filas
        for (int i = 0; i < departamentos.size(); i++) {
            String[] fila = new String[meses.size() + 1];
            fila[0] = departamentos.get(i);
            for (int j = 0; j < meses.size(); j++) {
                fila[j + 1] = Integer.toString(ventas[i][j]);
            }
            model.addRow(fila);
        }

        tabla.setModel(model);

        // Agregar un listener para detectar cambios en la tabla
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column != 0) {
                    Object value = model.getValueAt(row, column);
                    ventas[row][column - 1] = Integer.parseInt(value.toString());
                    guardarDatosEmpresa(rutaArchivo); // Guardar los cambios en el archivo
                }
            }
        });
    }

    private void cargarMeses() {
        comboBoxMeses.removeAllItems();
        for (String mes : meses) {
            comboBoxMeses.addItem(mes);
        }
    }

    private void leerDatosEmpresa(String rutaArchivo) throws IOException {
        this.rutaArchivo = rutaArchivo; // Guardar la ruta del archivo seleccionado

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            int fila = 0;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (fila == 0) {
                    for (int i = 1; i < datos.length; i++) {
                        meses.add(datos[i]);
                    }
                    ventas = new int[datos.length - 1][meses.size()];
                } else {
                    departamentos.add(datos[0]);
                    for (int i = 1; i < datos.length; i++) {
                        ventas[fila - 1][i - 1] = Integer.parseInt(datos[i]);
                    }
                }
                fila++;
            }
        }
    }

    private String seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de datos");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));
        int seleccion = fileChooser.showOpenDialog(this);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private void mostrarGrafica(String mesSeleccionado) {
        int indiceMes = meses.indexOf(mesSeleccionado);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < departamentos.size(); i++) {
            dataset.addValue(ventas[i][indiceMes], departamentos.get(i), "Departamento");
        }

        JFreeChart chart = ChartFactory.createBarChart("Dinero generado por departamento (" + mesSeleccionado + ")",
                "Departamento", "Dinero", dataset);

        ChartFrame frame = new ChartFrame("Gráfica de Barras (" + mesSeleccionado + ")", chart);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void guardarDatosEmpresa(String rutaArchivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Escribir los nombres de los meses en la primera línea
            bw.write("Departamento");
            for (String mes : meses) {
                bw.write("," + mes);
            }
            bw.newLine();

            // Escribir los datos de ventas por departamento en las siguientes líneas
            for (int i = 0; i < departamentos.size(); i++) {
                bw.write(departamentos.get(i));
                for (int j = 0; j < meses.size(); j++) {
                    bw.write("," + ventas[i][j]);
                }
                bw.newLine();
            }
            JOptionPane.showMessageDialog(null, "Datos guardados exitosamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar el archivo: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraficaDepartamentosGUI gui = new GraficaDepartamentosGUI();

                String rutaArchivo = gui.seleccionarArchivo();
                if (rutaArchivo != null) {
                    try {
                        gui.leerDatosEmpresa(rutaArchivo);
                        gui.cargarDatosTabla();
                        gui.cargarMeses();
                        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        gui.setSize(600, 400);
                        gui.setLocationRelativeTo(null);
                        gui.setVisible(true);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + e.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No se ha seleccionado un archivo válido.");
                }
            }
        });
    }
}

