package com.vsv.toasts;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

public final class Toasts {

    private Toasts() {

    }

    public static void nothingToAdd() {
        shortShow(R.string.toast_nothing_to_add);
    }

    public static void nothingToSave() {
        shortShow(R.string.toast_nothing_to_save);
    }

    public static void nothingToLoad() {
        shortShow(R.string.toast_nothing_to_load);
    }

    public static void shortShow(int resource) {
        show(resource, Toast.LENGTH_SHORT);
    }

    public static void show(String string, int length) {
        Context context = WeakContext.getContextOrNull();
        if (context != null) {
            Toast.makeText(context, string, length).show();
        }
    }

    public static void show(int resource, int length) {
        Context context = WeakContext.getContextOrNull();
        if (context != null) {
            Toast.makeText(context, resource, length).show();
        }
    }

    public static void noDictionariesToMove() {
        shortShow(R.string.no_dict_to_move);
    }

    public static void secondsRemain(float timeInSeconds) {
        shortShow(R.string.seconds_remain, timeInSeconds);
    }

    public static void unexpectedError() {
        shortShow(R.string.unexpected_error);
    }

    public static void shortShow(int resource, Object... objects) {
        shortShowRaw(StaticUtils.getString(resource, objects));
    }

    public static void longShow(int resource, Object... objects) {
        longShowRaw(StaticUtils.getString(resource, objects));
    }

    public static void tooFast() {
        shortShow(R.string.too_fast);
    }

    private static @NonNull
    String getString(int resource) {
        try {
            return WeakContext.getContext().getResources().getString(resource);
        } catch (Exception e) {
            return "";
        }
    }

    public static void cannotLoadGL() {
        longShow(R.string.load_gl_error);
    }

    public static void notEnoughTextureSize(int size) {
        longShow(R.string.load_texture_error, size);
    }

    public static void cannotEdit() {
        shortShow(R.string.toast_item_cannot_be_edited);
    }

    public static void shortShowRaw(String string) {
        Context context = WeakContext.getContextOrNull();
        if (context != null) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        }
    }

    public static void spreadsheetIdCopied() {
        shortShow(R.string.toast_copied);
    }

    public static void longShowRaw(String string) {
        Context context = WeakContext.getContextOrNull();
        if (context != null) {
            Toast.makeText(context, string, Toast.LENGTH_LONG).show();
        }
    }

    public static void cannotLoadSettings() {
        shortShow(R.string.toast_cannot_load_settings);
    }

    public static void success() {
        shortShow(R.string.toast_success);
    }

    public static void notAllSamplesAdded() {
        shortShow(R.string.toast_not_all_samples_added);
    }

    public static void languageNotSpecified() {
        shortShow(R.string.toast_language_not_specified);
    }

    public static void cannotReadBrokenFile() {
        shortShow(R.string.toast_broken_file);
    }

    public static void cannotOpenFile() {
        shortShow(R.string.toast_cannot_open_file);
    }

    public static void sheetNotSpecified() {
        shortShow(R.string.toast_spreadsheet_is_not_specified);
    }

    public static void setTheLanguage() {
        shortShow(R.string.set_the_language);
    }

    public static void sheetTabNotSpecified() {
        shortShow(R.string.toast_sheet_is_not_specified);
    }

    public static void chooseAnAction() {
        shortShow(R.string.toast_choose_action);
    }

    public static void chooseSpreadsheet() {
        shortShow(R.string.toast_choose_spreadsheet);
    }

    public static void chooseSheet() {
        shortShow(R.string.toast_choose_sheet);
    }

    public static void cannotCopyDictionary() {
        shortShow(R.string.toast_cannot_copy_dictionary);
    }

    public static void cannotAddDictionary() {
        shortShow(R.string.toast_cannot_add_dictionary);
    }

    public static void cannotAddShelf() {
        shortShow(R.string.toast_cannot_add_shelf);
    }

    public static void cannotAddSpreadsheet() {
        shortShow(R.string.toast_cannot_add_spreadsheet);
    }

    public static void cannotSendDictionary() {
        shortShow(R.string.toast_cannot_send_dictionary);
    }

    public static void cannotMergeDictionary() {
        shortShow(R.string.toast_cannot_merge_dictionary);
    }

    public static void cannotShareDictionary() {
        shortShow(R.string.toast_cannot_share_dictionary);
    }

    public static void cannotShareNotebook() {
        shortShow(R.string.toast_cannot_share_notebook);
    }

    public static void cannotShareSpreadsheet() {
        shortShow(R.string.toast_cannot_share_spreadsheet);
    }

    public static void cannotMoveDictionary() {
        shortShow(R.string.toast_cannot_move_dictionary);
    }

    public static void maxItemsMessage(String itemName) {
        shortShow(R.string.toast_maximum_items, itemName);
    }

    public static void somethingWentWrongCheckConnection() {
        longShow(R.string.toast_connect_to_spreadsheet);
    }

    public static void somethingWentWrongCheckConnection2() {
        longShow(R.string.toast_no_connection);
    }

    public static void somethingWentWrongWithAuth(String reason) {
        shortShow(R.string.sign_in_failed, reason);
    }

    public static void slowInternet() {
        shortShow(R.string.toast_slow_internet);
    }

    public static void somethingWentWrongTryLatter() {
        shortShow(R.string.toast_try_latter);
    }

    public static void wrongSheetTabName(String name) {
        shortShow(R.string.toast_sheet_not_exist);
    }

    public static void needLogin() {
        shortShow(R.string.toast_please_login);
    }

    public static void notEnoughSamples(int size) {
        shortShow(R.string.toast_not_enough_samples, size);
    }

    public static void sampleValueEmpty() {
        shortShow(R.string.toast_empty_sample);
    }

    public static void dictionaryNameEmpty() {
        shortShow(R.string.toast_dictionary_has_no_name);
    }

    public static void nameEmpty() {
        shortShow(R.string.toast_name_empty);
    }

    public static void noPresets() {
        shortShow(R.string.toast_no_presets);
    }

    public static void notebookNameEmpty() {
        shortShow(R.string.toast_notebook_name_empty);
    }

    public static void noteHeaderEmpty() {
        shortShow(R.string.toast_note_header_empty);
    }

    public static void noteContentEmpty() {
        shortShow(R.string.toast_note_content_empty);
    }

    public static void shelfNameEmpty() {
        shortShow(R.string.toast_shelf_has_no_name);
    }

    public static void sheetNameEmpty() {
        shortShow(R.string.toast_sheet_name_empty);
    }

    public static void spreadsheetNameEmpty() {
        shortShow(R.string.toast_spreadsheet_name_empty);
    }

    public static void spreadsheetIdEmpty() {
        shortShow(R.string.toast_spreadsheet_id_empty);
    }

    public static void spreadsheetExists() {
        shortShow(R.string.toast_spreadsheet_exists);
    }

    public static void readDictionaryError(String name) {
        shortShow(R.string.toast_wrong_reading_dictionary, name);
    }

    public static void readFromDictionaryError(String name) {
        longShow(R.string.toast_read_from_dictionary_error, name);
    }

    public static void readFromNotebookError(String name) {
        longShow(R.string.toast_read_from_notebook_error, name);
    }
}
