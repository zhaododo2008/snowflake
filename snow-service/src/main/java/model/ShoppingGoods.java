package model;

public class ShoppingGoods {
  
  public String getLast_price() {
    return last_price;
  }

  public void setLast_price( String last_price ) {
    this.last_price = last_price;
  }

  public String getBuy_number() {
    return buy_number;
  }

  public void setBuy_number( String buy_number ) {
    this.buy_number = buy_number;
  }

  public Integer getBuy_type() {
    return buy_type;
  }

  public void setBuy_type( Integer buy_type ) {
    this.buy_type = buy_type;
  }

  private String last_price;
  private String buy_number;

  /**
   * 购买类型 1:门店取货,2:线上购买 
   * 改为线上购买 ,防止下单时对  有货购买商品 ,塞入错误的值
   */
  private Integer buy_type = 2;
}
