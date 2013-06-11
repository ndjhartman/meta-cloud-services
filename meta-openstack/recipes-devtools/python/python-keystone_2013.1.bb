DESCRIPTION = "Authentication service for OpenStack"
HOMEPAGE = "http://www.openstack.org"
SECTION = "devel/python"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1dece7821bf3fd70fe1309eaa37d52a2"

PR = "r0"
SRCNAME = "keystone"

SRC_URI = "https://launchpad.net/keystone/grizzly/${PV}/+download/${SRCNAME}-${PV}.tar.gz \
           file://keystone.conf \
           file://identity.sh \
           file://openrc \
           file://keystone \
		  "

SRC_URI[md5sum] = "f82189cd7e3f0955e32c60e41f4120da"
SRC_URI[sha256sum] = "34347a3242a40d93b98c3722e6f3fbc112bc1c9ef20c045c3d40637e459b4574"

S = "${WORKDIR}/${SRCNAME}-${PV}"

inherit setuptools update-rc.d

SERVICE_TOKEN = "password"

do_install_append() {

    KEYSTONE_CONF_DIR=${D}${sysconfdir}/keystone

    install -d ${KEYSTONE_CONF_DIR}

    sed -e "s:^admin_token=.*:admin_token=${SERVICE_TOKEN}:g" -i ${WORKDIR}/keystone.conf

    install -m 600 ${WORKDIR}/keystone.conf ${KEYSTONE_CONF_DIR}/
    install -m 600 ${WORKDIR}/identity.sh ${KEYSTONE_CONF_DIR}/
    install -m 600 ${WORKDIR}/openrc ${KEYSTONE_CONF_DIR}/
    install -m 600 ${S}/etc/logging.conf.sample ${KEYSTONE_CONF_DIR}/logging.conf
    install -m 600 ${S}/etc/policy.json ${KEYSTONE_CONF_DIR}/policy.json

    if ${@base_contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/keystone ${D}${sysconfdir}/init.d/keystone
    fi

    # Create the sqlite database
    touch ${KEYSTONE_CONF_DIR}/keystone.db
}

pkg_postinst_${SRCNAME} () {

    if [ "x$D" != "x" ]; then
        exit 1
    fi

    # Needed when using a MySQL backend
    # mysql -u root -e "CREATE DATABASE keystone CHARACTER SET utf8;"
    sudo -u postgres createdb keystone
    keystone-manage db_sync
    keystone-manage pki_setup
    # quick fix
    echo "source /etc/keystone/openrc" > /home/root/.bashrc
    /etc/init.d/keystone start
    sleep 1
    bash /etc/keystone/identity.sh
}

PACKAGES += " ${SRCNAME}"

FILES_${PN} = "${libdir}/*"

FILES_${SRCNAME} = "${bindir}/* \
    ${sysconfdir}/${SRCNAME}/* \ 
    ${sysconfdir}/init.d/* "

RDEPENDS_${PN} += "python-pam \
	python-webob \
	python-eventlet \
	python-greenlet \
	python-pastedeploy \
	python-paste \
	python-routes \
	python-sqlalchemy \
	python-sqlalchemy-migrate \
	python-passlib \
	python-lxml \
	python-iso8601 \
	python-keystoneclient \
	python-oslo.config \
	"

RDEPENDS_${SRCNAME} = "${PN} \
        postgresql postgresql-client python-psycopg2"

INITSCRIPT_PACKAGES = "${SRCNAME}"
INITSCRIPT_NAME_${SRCNAME} = "keystone"
