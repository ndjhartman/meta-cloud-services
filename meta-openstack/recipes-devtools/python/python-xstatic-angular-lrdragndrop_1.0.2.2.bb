DESCRIPTION = "LrDragNDrop javascript library packaged for setuptools3"
HOMEPAGE = "https://pypi.python.org/pypi/XStatic-Angular-lrdragndrop"
SECTION = "devel/python"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://PKG-INFO;md5=92d5d5d8e51c0c2f4f6db4a084a59173"

PYPI_PACKAGE = "XStatic-Angular-lrdragndrop"

SRC_URI[md5sum] = "afd682cab9f436cf22b025dfcabaa225"
SRC_URI[sha256sum] = "1cf04495981db5dfd5536441e17ec69bb18d624f847ddc203f3259d81b10a77e"

inherit setuptools3 pypi

DEPENDS += " \
        python-pip \
        "

RDEPENDS_${PN} += " \
        "
