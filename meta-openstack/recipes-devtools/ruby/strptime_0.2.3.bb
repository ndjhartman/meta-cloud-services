SUMMARY = "a fast strpitme engine"
HOMEPAGE = "https://github.com/nurse/strptime"

LICENSE = "other"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=f19575067ffc5f1ddc02c74eeef9904f"

SRC_URI = "git://github.com/nurse/strptime.git;protocol=https;tag=v0.2.3"

S = "${WORKDIR}/git"

RDEPENDS_${PN} = "bash"

inherit ruby
