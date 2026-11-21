package qlbh;

/** Phần tử (mã, tên) dùng cho ComboBox. */
public record Item(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
