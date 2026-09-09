/**
 * Convierte los informes en Markdown de esta carpeta a documentos Word.
 *
 *   node generar_docx.js
 *
 * Requiere el paquete "docx" (npm install -g docx).
 * Marcadores propios admitidos en el Markdown:
 *   [PORTADA] / [PORTADA2]  caratula del informe o del guion
 *   [TOC]                   indice automatico
 *   [SALTO]                 salto de pagina
 *   [IMG:ruta]              imagen
 *   [PIE:texto]             pie de figura
 */
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, ImageRun,
  Footer, AlignmentType, LevelFormat, TableOfContents, HeadingLevel, BorderStyle,
  WidthType, ShadingType, PageNumber, PageBreak, VerticalAlign,
} = require('docx');

const ANCHO = 9026;            // A4 con margenes de una pulgada
const DIR = __dirname;

// ---------------------------------------------------------------- utilidades

/** Divide un texto en runs aplicando **negrita**, *cursiva* y `codigo`. */
function runs(texto, base = {}) {
  const salida = [];
  const patron = /(\*\*[^*]+\*\*|\*[^*]+\*|`[^`]+`)/g;
  let ultimo = 0, m;
  while ((m = patron.exec(texto)) !== null) {
    if (m.index > ultimo) salida.push(new TextRun({ ...base, text: texto.slice(ultimo, m.index) }));
    const t = m[0];
    if (t.startsWith('**')) salida.push(new TextRun({ ...base, text: t.slice(2, -2), bold: true }));
    else if (t.startsWith('`')) salida.push(new TextRun({ ...base, text: t.slice(1, -1), font: 'Consolas', size: 19 }));
    else salida.push(new TextRun({ ...base, text: t.slice(1, -1), italics: true }));
    ultimo = m.index + t.length;
  }
  if (ultimo < texto.length) salida.push(new TextRun({ ...base, text: texto.slice(ultimo) }));
  return salida.length ? salida : [new TextRun({ ...base, text: '' })];
}

const borde = { style: BorderStyle.SINGLE, size: 1, color: 'B4B4B4' };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };

function celda(texto, ancho, encabezado) {
  return new TableCell({
    borders: bordes,
    width: { size: ancho, type: WidthType.DXA },
    shading: encabezado ? { fill: 'E8EDF2', type: ShadingType.CLEAR } : undefined,
    margins: { top: 60, bottom: 60, left: 110, right: 110 },
    verticalAlign: VerticalAlign.CENTER,
    children: [new Paragraph({
      spacing: { before: 0, after: 0 },
      children: runs(texto, { size: 20, bold: encabezado || undefined }),
    })],
  });
}

function tabla(filas) {
  const columnas = filas[0].length;
  const base = Math.floor(ANCHO / columnas);
  const anchos = Array(columnas).fill(base);
  anchos[columnas - 1] += ANCHO - base * columnas;
  return new Table({
    width: { size: ANCHO, type: WidthType.DXA },
    columnWidths: anchos,
    rows: filas.map((fila, i) => new TableRow({
      tableHeader: i === 0,
      children: fila.map((c, j) => celda(c, anchos[j], i === 0)),
    })),
  });
}

function lineaCodigo(texto) {
  return new Paragraph({
    spacing: { before: 0, after: 0 },
    shading: { fill: 'F4F5F7', type: ShadingType.CLEAR },
    indent: { left: 200 },
    children: [new TextRun({ text: texto || ' ', font: 'Consolas', size: 17 })],
  });
}

function celdaPortada(etiqueta, valor) {
  return new TableRow({
    children: [
      new TableCell({
        borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
        width: { size: 2800, type: WidthType.DXA },
        margins: { top: 60, bottom: 60, left: 0, right: 120 },
        children: [new Paragraph({ children: [new TextRun({ text: etiqueta, bold: true, size: 22 })] })],
      }),
      new TableCell({
        borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
        width: { size: 6226, type: WidthType.DXA },
        margins: { top: 60, bottom: 60, left: 0, right: 0 },
        children: [new Paragraph({ children: runs(valor, { size: 22 }) })],
      }),
    ],
  });
}

/** Caratula academica. */
function portada(titulo, subtitulo) {
  const p = [];
  p.push(new Paragraph({ spacing: { after: 900 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: 'UNIVERSIDAD TECNOLÓGICA DEL PERÚ', bold: true, size: 28 })] }));
  p.push(new Paragraph({ spacing: { after: 200 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: 'FACULTAD DE INGENIERÍA', size: 24 })] }));
  p.push(new Paragraph({ spacing: { after: 1400 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: 'CURSO: DESARROLLO WEB INTEGRADO', size: 24 })] }));
  p.push(new Paragraph({ spacing: { after: 200 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: titulo, bold: true, size: 40 })] }));
  p.push(new Paragraph({ spacing: { after: 1400 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: subtitulo, size: 26, italics: true })] }));

  p.push(new Table({
    width: { size: ANCHO, type: WidthType.DXA },
    columnWidths: [2800, 6226],
    rows: [
      celdaPortada('Integrantes:', 'Integrante 1 — [Apellidos, Nombres]'),
      celdaPortada('', 'Integrante 2 — [Apellidos, Nombres]'),
      celdaPortada('', 'Integrante 3 — [Apellidos, Nombres]'),
      celdaPortada('', 'Integrante 4 — [Apellidos, Nombres]'),
      celdaPortada('', 'Integrante 5 — [Apellidos, Nombres]'),
      celdaPortada('', 'Integrante 6 — [Apellidos, Nombres]'),
      celdaPortada('Docente:', '[Nombre del docente]'),
      celdaPortada('Sección:', '[Sección]'),
      celdaPortada('Institución:', 'Universidad Tecnológica del Perú'),
      celdaPortada('Fecha de entrega:', '[día] de setiembre de 2026'),
    ],
  }));
  p.push(new Paragraph({ spacing: { before: 1400 }, alignment: AlignmentType.CENTER, children: [
    new TextRun({ text: 'Lima — Perú', size: 24 })] }));
  p.push(new Paragraph({ children: [new PageBreak()] }));
  return p;
}

// ------------------------------------------------------------- conversion md

function convertir(md, titulo, subtitulo) {
  const lineas = md.split(/\r?\n/);
  const hijos = [];
  let i = 0;

  while (i < lineas.length) {
    const linea = lineas[i];

    if (linea.trim() === '[PORTADA]' || linea.trim() === '[PORTADA2]') {
      hijos.push(...portada(titulo, subtitulo)); i++; continue;
    }
    if (linea.trim() === '[TOC]') {
      hijos.push(new TableOfContents('Contenido', { hyperlink: true, headingStyleRange: '1-3' }));
      i++; continue;
    }
    if (linea.trim() === '[SALTO]') {
      hijos.push(new Paragraph({ children: [new PageBreak()] })); i++; continue;
    }
    const img = linea.match(/^\[IMG:(.+)\]$/);
    if (img) {
      const ruta = path.join(DIR, img[1].trim());
      if (fs.existsSync(ruta)) {
        hijos.push(new Paragraph({
          alignment: AlignmentType.CENTER, spacing: { before: 200, after: 60 },
          children: [new ImageRun({
            type: 'png', data: fs.readFileSync(ruta),
            transformation: { width: 440, height: 275 },
            altText: { title: 'Captura', description: 'Captura de la aplicacion', name: 'Captura' },
          })],
        }));
      }
      i++; continue;
    }
    const pie = linea.match(/^\[PIE:(.+)\]$/);
    if (pie) {
      hijos.push(new Paragraph({
        alignment: AlignmentType.CENTER, spacing: { after: 240 },
        children: [new TextRun({ text: pie[1].trim(), size: 18, italics: true, color: '555555' })],
      }));
      i++; continue;
    }

    // Bloque de codigo
    if (linea.trim().startsWith('```')) {
      i++;
      hijos.push(new Paragraph({ spacing: { before: 120, after: 0 }, children: [new TextRun('')] }));
      while (i < lineas.length && !lineas[i].trim().startsWith('```')) {
        hijos.push(lineaCodigo(lineas[i])); i++;
      }
      i++;
      hijos.push(new Paragraph({ spacing: { before: 0, after: 160 }, children: [new TextRun('')] }));
      continue;
    }

    // Tabla
    if (linea.trim().startsWith('|') && i + 1 < lineas.length && /^\s*\|[\s:|-]+\|\s*$/.test(lineas[i + 1])) {
      const filas = [];
      const celdas = (l) => l.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((c) => c.trim());
      filas.push(celdas(lineas[i])); i += 2;
      while (i < lineas.length && lineas[i].trim().startsWith('|')) { filas.push(celdas(lineas[i])); i++; }
      hijos.push(tabla(filas));
      hijos.push(new Paragraph({ spacing: { after: 160 }, children: [new TextRun('')] }));
      continue;
    }

    // Encabezados
    const enc = linea.match(/^(#{1,4})\s+(.*)$/);
    if (enc) {
      const nivel = [HeadingLevel.HEADING_1, HeadingLevel.HEADING_2, HeadingLevel.HEADING_3, HeadingLevel.HEADING_4][enc[1].length - 1];
      hijos.push(new Paragraph({ heading: nivel, children: runs(enc[2]) }));
      i++; continue;
    }

    // Listas
    const vinieta = linea.match(/^[-*]\s+(.*)$/);
    if (vinieta) {
      hijos.push(new Paragraph({ numbering: { reference: 'vinetas', level: 0 }, spacing: { after: 80 }, children: runs(vinieta[1]) }));
      i++; continue;
    }
    const numerada = linea.match(/^\d+\.\s+(.*)$/);
    if (numerada) {
      hijos.push(new Paragraph({ numbering: { reference: 'numeros', level: 0 }, spacing: { after: 80 }, children: runs(numerada[1]) }));
      i++; continue;
    }

    if (linea.trim() === '') { i++; continue; }

    hijos.push(new Paragraph({
      alignment: AlignmentType.JUSTIFIED, spacing: { after: 140, line: 300 },
      children: runs(linea.trim()),
    }));
    i++;
  }
  return hijos;
}

function documento(hijos) {
  return new Document({
    styles: {
      default: { document: { run: { font: 'Arial', size: 22 } } },
      paragraphStyles: [
        { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 30, bold: true, font: 'Arial', color: '1F3864' },
          paragraph: { spacing: { before: 320, after: 200 }, outlineLevel: 0 } },
        { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 26, bold: true, font: 'Arial', color: '2E4F7C' },
          paragraph: { spacing: { before: 260, after: 160 }, outlineLevel: 1 } },
        { id: 'Heading3', name: 'Heading 3', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 23, bold: true, font: 'Arial' },
          paragraph: { spacing: { before: 220, after: 140 }, outlineLevel: 2 } },
        { id: 'Heading4', name: 'Heading 4', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: 22, bold: true, italics: true, font: 'Arial' },
          paragraph: { spacing: { before: 180, after: 120 }, outlineLevel: 3 } },
      ],
    },
    numbering: {
      config: [
        { reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 620, hanging: 320 } } } }] },
        { reference: 'numeros', levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1.', alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 620, hanging: 320 } } } }] },
      ],
    },
    sections: [{
      properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
      footers: { default: new Footer({ children: [new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: '', size: 18 }), new TextRun({ children: [PageNumber.CURRENT], size: 18 })],
      })] }) },
      children: hijos,
    }],
  });
}

const trabajos = [
  { md: 'Informe_Proyecto_Final_DWI.md', docx: 'Informe_Proyecto_Final_DWI.docx',
    titulo: 'SISTEMA DE GESTIÓN ODONTOLÓGICA',
    subtitulo: 'Aplicación web con API RESTful en Spring Boot y front-end en Angular — Informe del Proyecto Final' },
  { md: 'Guion_Exposicion_Semanas_1_a_4.md', docx: 'Guion_Exposicion_Semanas_1_a_4.docx',
    titulo: 'GUION DE EXPOSICIÓN DEL PROYECTO',
    subtitulo: 'Reparto por integrante según los temas de las semanas 1 a 4' },
];

(async () => {
  for (const t of trabajos) {
    const md = fs.readFileSync(path.join(DIR, t.md), 'utf8');
    const doc = documento(convertir(md, t.titulo, t.subtitulo));
    const buffer = await Packer.toBuffer(doc);
    fs.writeFileSync(path.join(DIR, t.docx), buffer);
    console.log('generado:', t.docx, (buffer.length / 1024).toFixed(0) + ' KB');
  }
})();
