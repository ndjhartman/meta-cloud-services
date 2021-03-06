DESCRIPTION = "System V IPC primitives (semaphores, shared memory and message queues) for Python"
HOMEPAGE = "http://semanchuk.com/philip/sysv_ipc/"
SECTION = "devel/python"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://PKG-INFO;md5=9d6e34e7b47096d7c19c1e3be707374e"

PYPI_PACKAGE = "sysv_ipc"

SRC_URI[md5sum] = "c6cf5b4aa7cd3e07fd4b5661530eca8c"
SRC_URI[sha256sum] = "0af73375a85c5d9d487c2f14f208812600bd81e4046437ebaf55746b3aade00e"

inherit pypi

# DEPENDS_default: python-pip

DEPENDS += " \
        ${PYTHON_PN}-pip \
        "

# RDEPENDS_default: 
RDEPENDS_${PN} += " \
        "
