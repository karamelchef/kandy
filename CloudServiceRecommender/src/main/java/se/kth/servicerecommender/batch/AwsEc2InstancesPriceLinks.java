package se.kth.servicerecommender.batch;

/**
 *
 * @author Hossein
 */
public enum AwsEc2InstancesPriceLinks {

  ONDEMAND_LINUX("http://a0.awsstatic.com/pricing/1/ec2/linux-od.min.js", "Linux"),
  //  ONDEMAND_RHEL("http://a0.awsstatic.com/pricing/1/ec2/rhel-od.min.js"),
  //  ONDEMAND_SLES("http://a0.awsstatic.com/pricing/1/ec2/sles-od.min.js"),
  //  ONDEMAND_WINDOWS("http://a0.awsstatic.com/pricing/1/ec2/mswin-od.min.js"),
  //  ONDEMAND_WINDOWS_SQL_STANDRAD("http://a0.awsstatic.com/pricing/1/ec2/mswinSQL-od.min.js"),
  //  ONDEMAND_WINDOWS_SQL_WEB("http://a0.awsstatic.com/pricing/1/ec2/mswinSQLWeb-od.min.js"),
  //  ONDEMAND_WINDOWS_SQL_ENTERPRISE("http://a0.awsstatic.com/pricing/1/ec2/mswinSQLEnterprise-od.min.js"),
  RESERVED_LINUX("http://a0.awsstatic.com/pricing/1/ec2/ri-v2/linux-unix-shared.min.js", "Linux"),
  RESERVED_RHEL("http://a0.awsstatic.com/pricing/1/ec2/ri-v2/red-hat-enterprise-linux-shared.min.js", "RHEL"),
  RESERVED_SLES("http://a0.awsstatic.com/pricing/1/ec2/ri-v2/suse-linux-shared.min.js", "SLES"),
  RESERVED_WINDOWS("http://a0.awsstatic.com/pricing/1/ec2/ri-v2/windows-shared.min.js", "Windows"),
  RESERVED_WINDOWS_SQL_STANDRAD(
      "http://a0.awsstatic.com/pricing/1/ec2/ri-v2/windows-with-sql-server-standard-shared.min.js",
      "Windows_Sql_Standard"),
  RESERVED_WINDOWS_SQL_WEB("http://a0.awsstatic.com/pricing/1/ec2/ri-v2/windows-with-sql-server-web-shared.min.js",
      "Windows_Sql_Web"),
  RESERVED_WINDOWS_SQL_ENTERPRISE(
      "http://a0.awsstatic.com/pricing/1/ec2/ri-v2/windows-with-sql-server-enterprise-shared.min.js",
      "Windows_Sql_Enterprise");

  private final String link;
  private final String OSType;

  private AwsEc2InstancesPriceLinks(String link, String osType) {
    this.link = link;
    this.OSType = osType;
  }

  public String getLink() {
    return link;
  }

  public String getOSType() {
    return OSType;
  }
}
