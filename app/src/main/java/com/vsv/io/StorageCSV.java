package com.vsv.io;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vsv.db.entities.DataToAdd;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.NotebookWithNotes;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.CodeNames;
import com.vsv.utils.SheetDataBuilder;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class StorageCSV {

    public static final String EXTENSION = ".csv";

    private final File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

    public StorageCSV() {

    }

    public boolean checkOrCreateDictionariesFolder() {
        if (!root.exists()) {
            return root.mkdir();
        }
        return true;
    }

    public ArrayList<File> getAllDictionaryFilesAsList() {
        boolean result = this.checkOrCreateDictionariesFolder();
        if (!result) {
            return null;
        }
        File[] names = this.root.listFiles((dir, name) -> name.toLowerCase().endsWith(EXTENSION));
        if (names == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(names));
    }

    public boolean removeDictionaryFile(String name) {
        File file = new File(root, name + EXTENSION);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    public boolean createDictionaryFile(String name) {
        File file = new File(root, name + EXTENSION);
        try {
            return file.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }

    public CSVReader getDictionaryReader(String name) {
        File file = new File(root, name + EXTENSION);
        try {
            return new CSVReader(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public boolean addSample(String dictionaryName, String leftValue, String rightValue) {
        CSVWriter writer = getCSVWriter(dictionaryName, true);
        if (writer == null) {
            return false;
        }
        writer.writeNext(new String[]{leftValue, rightValue});
        try {
            writer.flush();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                //
            }
        }
        return true;
    }

    public boolean saveSamples(@NonNull String fileName, @Nullable ArrayList<Sample> samples) {
        if (samples == null || samples.isEmpty()) {
            return false;
        }
        CSVWriter writer = getCSVWriter(fileName, false);
        if (writer == null) {
            return false;
        }
        for (Sample sample : samples) {
            String kind = sample.getType();
            String example = sample.getExample();
            writer.writeNext(new String[]{sample.getLeftValue(), sample.getRightValue(), kind, example});
        }
        try {
            writer.flush();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                //
            }
        }
        return true;
    }

    public @Nullable
    CSVWriter getCSVWriter(String name, boolean append) {
        return getCSVWriter(new File(root, name + EXTENSION), append);
    }

    public @Nullable
    CSVWriter getCSVWriter(File file, boolean append) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_16);
            return new CSVWriter(new BufferedWriter(outputStreamWriter));
        } catch (IOException e) {
            Log.e("Storage", e.toString());
            return null;
        }
    }

    public @Nullable
    CSVReader getReaderFromContentResolver(@Nullable String uriData, @NonNull Context context) {
        if (uriData == null) {
            return null;
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uriData));
            if (inputStream != null) {
                return new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_16));
            } else {
                Log.e("CSV READER", "Input stream for: " + uriData + " is null");
            }
        } catch (FileNotFoundException e) {
            Log.e("CSV READER", e.toString());
            return null;
        }
        return null;
    }

    public @NonNull
    CSVWriter getCSWMemoryWriter(@NonNull ByteArrayOutputStream stream) {
        return new CSVWriter(new BufferedWriter(new OutputStreamWriter(stream)));
    }

    public static void sendFile(@Nullable File sendFile) {
        if (sendFile == null) {
            return;
        }
        Context context = WeakContext.getContext();
        Uri contentUri = FileProvider.getUriForFile(context, "com.vsv.memorizer.fileprovider", sendFile);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        sendIntent.setType("text/csv");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void sendText(String text) {
        Context context = WeakContext.getContext();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    @NonNull
    public static File buildOrCleanDocFolder() throws IOException {
        Context context = WeakContext.getContext();
        File csvPath = new File(context.getExternalFilesDir(null), "csv_docs");
        boolean result = csvPath.exists();
        if (!result) {
            result = csvPath.mkdir();
            if (result) {
                return csvPath;
            } else {
                throw new IOException("Can not create folder by path: " + csvPath.getAbsolutePath());
            }
        } else {
            FileUtils.cleanDirectory(csvPath);
        }
        return csvPath;
    }

    // Weak context should be initialized first
    public static void buildOrCleanDocFolderSilence() {
        try {
            buildOrCleanDocFolder();
        } catch (IOException e) {
            Log.e("BuildDocFolder", e.toString());
        }
    }

    @Nullable
    public static File createTemporaryFile(int failUserMessage, String fileName) {
        File sendFile;
        try {
            File docFolder = buildOrCleanDocFolder();
            String sendFileName = fileName + StorageCSV.EXTENSION;
            sendFile = new File(docFolder, sendFileName);
            boolean result = sendFile.exists();
            if (!result) {
                result = sendFile.createNewFile();
            }
            if (!result) {
                Toasts.shortShow(failUserMessage);
                return null;
            }
        } catch (IOException e) {
            Log.e("Sender", e.toString());
            Toasts.shortShow(failUserMessage);
            return null;
        }
        return sendFile;
    }

    public boolean loadDictionaryWithSamplesIntoFile(@NonNull DictionaryWithSamples dictionaryWithSamples, @Nullable File file) {
        if (file == null) {
            return false;
        }
        CSVWriter writer = getCSVWriter(file, false);
        if (writer != null) {
            return loadDictionaryWithSamplesIntoCSVWriter(dictionaryWithSamples, writer);
        }
        return false;
    }

    public boolean loadDictionaryIntoFile(@NonNull Dictionary dictionary, @Nullable File file) {
        if (file == null) {
            return false;
        }
        CSVWriter writer = getCSVWriter(file, false);
        if (writer != null) {
            return loadDictionaryIntoCSVWriter(dictionary, writer);
        }
        return false;
    }

    public boolean loadNotebookSSIntoFile(@NonNull Notebook notebook, @Nullable File file) {
        if (file == null) {
            return false;
        }
        CSVWriter writer = getCSVWriter(file, false);
        if (writer != null) {
            return loadNotebookIntoCSVWriter(notebook, writer);
        }
        return false;
    }

    public boolean loadSpreadsheetIntoFile(@NonNull SpreadSheetInfo spreadsheet, @Nullable File file) {
        if (file == null) {
            return false;
        }
        CSVWriter writer = getCSVWriter(file, false);
        if (writer != null) {
            return loadSpreadsheetIntoCSVWriter(spreadsheet, writer);
        }
        return false;
    }

    public boolean loadNotebookWithNotesIntoFile(@NonNull Notebook notebook, @NonNull ArrayList<Note> notes, @Nullable File file) {
        if (file == null) {
            return false;
        }
        CSVWriter writer = getCSVWriter(file, false);
        if (writer != null) {
            return loadNotebookWithNotesIntoCSVWriter(notebook, notes, writer);
        }
        return false;
    }

    private boolean loadNotebookIntoCSVWriter(@NonNull Notebook notebook, @NonNull CSVWriter writer) {
        writer.writeNext(getNotebookHeader(notebook, false)); // We do not need to setup the date because we do not have samples.
        try {
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    private boolean loadNotebookWithNotesIntoCSVWriter(@NonNull Notebook notebook, @NonNull ArrayList<Note> notes, @NonNull CSVWriter writer) {
        writer.writeNext(getNotebookHeader(notebook, true));
        notes.sort(Comparator.comparingInt(Note::getNumber));
        for (Note note : notes) {
            writer.writeNext(new String[]{note.getName(), note.getContent()});
        }
        try {
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    private boolean loadSpreadsheetIntoCSVWriter(@NonNull SpreadSheetInfo spreadsheet, @NonNull CSVWriter writer) {
        String[] header = new String[]{CodeNames.SPREADSHEET, spreadsheet.name, spreadsheet.spreadSheetId, String.valueOf(spreadsheet.type)};
        writer.writeNext(header);
        try {
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    private String[] getDictionaryHeader(@NonNull Dictionary dictionary, boolean setDate) {
        String name = dictionary.getName() == null ? StaticUtils.getString(R.string.new_dictionary) : dictionary.getName();
        String[] header;
        if (dictionary.hasOwner()) {
            String date = dictionary.dataDate == null || !setDate ? "" : String.valueOf(dictionary.dataDate.getTime());
            String spreadsheetName = dictionary.spreadsheetName == null ? "" : dictionary.spreadsheetName;
            String spreadsheetId = dictionary.spreadsheetId == null ? "" : dictionary.spreadsheetId;
            header = new String[]{CodeNames.DICT, name, dictionary.getLeftLocaleAbb(), dictionary.getRightLocaleAbb(),
                    date, spreadsheetName, spreadsheetId, dictionary.sheetName, String.valueOf(dictionary.sheetId)};
        } else {
            header = new String[]{CodeNames.DICT, name, dictionary.getLeftLocaleAbb(), dictionary.getRightLocaleAbb()};
        }
        return header;
    }

    private String[] getNotebookHeader(@NonNull Notebook notebook, boolean setDate) {
        String name = notebook.getName() == null ? StaticUtils.getString(R.string.default_notebook_name) : notebook.getName();
        String[] header;
        if (notebook.hasOwner()) {
            String date = notebook.dataDate == null || !setDate ? "" : String.valueOf(notebook.dataDate.getTime());
            String spreadsheetName = notebook.spreadsheetName == null ? "" : notebook.spreadsheetName;
            String spreadsheetId = notebook.spreadsheetId == null ? "" : notebook.spreadsheetId;
            header = new String[]{CodeNames.NOTE, name, spreadsheetName, spreadsheetId, date, notebook.sheetName, String.valueOf(notebook.sheetId)};
        } else {
            header = new String[]{CodeNames.NOTE, name};
        }
        return header;
    }

    private boolean loadDictionaryIntoCSVWriter(@NonNull Dictionary dictionary, @NonNull CSVWriter writer) {
        writer.writeNext(getDictionaryHeader(dictionary, false)); // We do not need to setup the date because we do not have samples.
        try {
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    private boolean loadDictionaryWithSamplesIntoCSVWriter(@NonNull DictionaryWithSamples dictionaryWithSamples, @NonNull CSVWriter writer) {
        assert dictionaryWithSamples.dictionary != null;
        writer.writeNext(getDictionaryHeader(dictionaryWithSamples.dictionary, true));
        if (dictionaryWithSamples.samples != null) {
            for (Sample sample : dictionaryWithSamples.samples) {
                writer.writeNext(new String[]{sample.getLeftValue(), sample.getRightValue(), sample.getType(), sample.getExample()});
            }
        }
        try {
            writer.flush();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    public @Nullable
    ByteArrayOutputStream convertDictionaryWithSamplesToStream(@NonNull DictionaryWithSamples dictionaryWithSamples) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CSVWriter writer = Storage.getDocumentsStorage().getCSWMemoryWriter(stream);
        boolean result = loadDictionaryWithSamplesIntoCSVWriter(dictionaryWithSamples, writer);
        return result ? stream : null;
    }

    public @Nullable
    DataToAdd getDataFromContentResolver(@Nullable String uriData, @NonNull Context context) {
        if (uriData == null) {
            return null;
        }
        CSVReader reader = getReaderFromContentResolver(uriData, context);
        ArrayList<Sample> samples = new ArrayList<>();
        ArrayList<Note> notes = new ArrayList<>();
        Dictionary dictionary = null;
        Notebook notebook = null;
        SpreadSheetInfo spreadsheet = null;
        if (reader == null) {
            Log.e("CSV READER", "reader is null");
        } else {
            int linesCount = 0;
            try {
                String[] nextLine;
                boolean end = false;
                int samplesCount = 0;
                int notesCount = 0;
                boolean checkHeader = false;
                while (!end && (nextLine = reader.readNext()) != null) {
                    linesCount++;

                    // Create dictionary if the data is specified.
                    if (!checkHeader) {
                        List<String> line = Arrays.asList(nextLine);
                        checkHeader = true;
                        if (SheetDataBuilder.isNoteHeader(line)) {
                            SheetDataBuilder.NotebookData notebookData = SheetDataBuilder.getNotebookData(line, null, SheetDataBuilder.SHEET_DATA_CSV);
                            assert notebookData != null;
                            notebook = new Notebook(notebookData.notebookName);
                            notebook.canCopy = notebookData.canCopy;
                            notebook.sheetName = notebookData.sheetName;
                            notebook.spreadsheetName = notebookData.spreadsheetName;
                            notebook.dataDate = notebookData.dataDate;
                            notebook.spreadsheetId = notebookData.spreadsheetId;
                            notebook.sheetId = notebookData.sheetId;
                            notebook.author = notebookData.author;
                            continue;
                        } else if (SheetDataBuilder.isSpreadsheetHeader(line)) {
                            SheetDataBuilder.SpreadsheetData spreadsheetData = SheetDataBuilder.getSpreadsheetData(line);
                            spreadsheet = new SpreadSheetInfo(spreadsheetData.spreadsheetId, spreadsheetData.sheetName, spreadsheetData.type);
                        } else {
                            boolean isHeader = false;
                            SheetDataBuilder.DictData dictData = SheetDataBuilder.getDictData(line, null, SheetDataBuilder.SHEET_DATA_CSV);
                            if (dictData == null) {
                                dictData = SheetDataBuilder.getDefaultDictData(null);
                            } else {
                                isHeader = true;
                            }
                            dictionary = new Dictionary(0, dictData.dictName);
                            dictionary.setLeftLocaleAbb(dictData.leftLanguage);
                            dictionary.setRightLocaleAbb(dictData.rightLanguage);
                            dictionary.canCopy = dictData.canCopy;
                            dictionary.spreadsheetName = dictData.spreadsheetName;
                            dictionary.dataDate = dictData.dataDate;
                            dictionary.spreadsheetId = dictData.spreadsheetId;
                            dictionary.sheetName = dictData.sheetName;
                            dictionary.sheetId = dictData.sheetId;
                            dictionary.author = dictData.author;
                            if (isHeader) {
                                continue;
                            }
                        }
                    }
                    if (notebook != null) {
                        if (SheetDataBuilder.addNote(Arrays.asList(nextLine), notes, notes.size())) {
                            notesCount++;
                        }
                        if (notesCount == Spec.MAX_NOTES) {
                            end = true;
                        }
                    } else if (spreadsheet != null) {
                        break;
                    } else {
                        if (SheetDataBuilder.addSample(Arrays.asList(nextLine), samples, false)) {
                            samplesCount++;
                        }
                        if (samplesCount == Spec.MAX_SAMPLES) {
                            end = true;
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("CSV READER", "Read lines: " + linesCount + "\n" + e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("CSV READER", e.toString());
                }
            }
        }
        DataToAdd dataToAdd = new DataToAdd();
        if (notebook != null) {
            NotebookWithNotes notebookWithNotes = new NotebookWithNotes();
            notebookWithNotes.notebook = notebook;
            notebookWithNotes.notes = notes;
            dataToAdd.notebookWithNotes = notebookWithNotes;
        } else if (spreadsheet != null) {
            dataToAdd.spreadsheet = spreadsheet;
        } else {
            if (dictionary == null) {
                dictionary = Dictionary.buildDefault(null);
            }
            DictionaryWithSamples dictionaryWithSamples = new DictionaryWithSamples();
            dictionary.setCount(samples.size());
            dictionaryWithSamples.dictionary = dictionary;
            if (samples.isEmpty() && dictionary.hasOwner()) {
                // We reset the sheet update time because dictionary has the owner but has no samples.
                dictionary.dataDate = null;
            }
            dictionaryWithSamples.samples = samples;
            dataToAdd.dictionaryWithSamples = dictionaryWithSamples;
        }
        return dataToAdd;
    }

    public String[] getAllDictionaryNames(ArrayList<File> files) {
        if (files == null) {
            return null;
        }
        if (files.isEmpty()) {
            return new String[0];
        }
        String[] names = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            names[i] = getNameWithoutExtension(files.get(i));
        }
        return names;
    }

    private String getNameWithoutExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index > -1) {
            name = name.substring(0, index);
        }
        return name;
    }

    public ArrayList<String> getAllDictionaryNamesAsList(ArrayList<File> files) {
        if (files == null) {
            return null;
        }
        if (files.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(getNameWithoutExtension(file));
        }
        return names;
    }
}
