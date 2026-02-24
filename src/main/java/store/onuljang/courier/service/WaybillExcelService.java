package store.onuljang.courier.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.entity.CourierOrderItem;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WaybillExcelService {

    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generateWaybillExcel(CourierOrder order) {
        return generateWaybillExcel(List.of(order));
    }

    public byte[] generateWaybillExcel(List<CourierOrder> orders) {
        try (InputStream is = getClass().getResourceAsStream("/templates/waybill-template.xlsx");
                Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowIdx = 1;

            for (CourierOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(order.getReceiverName()));
                row.createCell(1).setCellValue(nullSafe(order.getReceiverPhone()));
                row.createCell(2).setCellValue("");
                row.createCell(3).setCellValue(nullSafe(order.getAddress1()));
                row.createCell(4).setCellValue(nullSafe(order.getAddress2()));
                row.createCell(5).setCellValue(order.getProductSummary());
                row.createCell(6).setCellValue(getOptionSummary(order));
                row.createCell(7).setCellValue(order.getTotalQuantity());
                row.createCell(8).setCellValue(nullSafe(order.getShippingMemo()));
                row.createCell(9).setCellValue(
                        order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "");
                row.createCell(10).setCellValue(order.getDisplayCode());
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("운송장 Excel 생성 실패", e);
        }
    }

    private String getOptionSummary(CourierOrder order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "";
        }
        CourierOrderItem first = order.getItems().get(0);
        return nullSafe(first.getSelectedOptions());
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
