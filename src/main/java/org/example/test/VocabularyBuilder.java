package org.example.test;

import org.apache.poi.ss.usermodel.*;

import java.util.HashSet;
import java.util.Set;

public class VocabularyBuilder {
    private Set<String> vocabulary;

    public VocabularyBuilder(Workbook... workbooks) {
        this.vocabulary = new HashSet<>();
        for (Workbook workbook : workbooks) {
            buildVocabulary(workbook);
        }
    }


    private void buildVocabulary(Workbook workbook) {
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String[] words = cell.getStringCellValue().split("\\s+");
                        for (String word : words) {
                            vocabulary.add(word.toLowerCase());
                        }
                    }
                }
            }
        }
    }

    public Set<String> getVocabulary() {
        return vocabulary;
    }
}