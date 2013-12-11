DESCRIPTION = "OpenStack Metering Component"
HOMEPAGE = "https://launchpad.net/ceilometer"
SECTION = "devel/python"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1dece7821bf3fd70fe1309eaa37d52a2"

PR = "r0"
SRCNAME = "ceilometer"

SRC_URI = "git://github.com/openstack/${SRCNAME}.git;branch=stable/havana \
           file://ceilometer.conf \
           file://ceilometer.init \
"

SRCREV="b6165624ef1095e979b4e383a84462afb62ec2d0"
PV="2013.2+git${SRCPV}"
S = "${WORKDIR}/git"

CEILOMETER_SECRET ?= "12121212"

do_install_append() {
    TEMPLATE_CONF_DIR=${S}${sysconfdir}/${SRCNAME}
    CEILOMETER_CONF_DIR=${D}${sysconfdir}/${SRCNAME}

    sed -e "s:%CEILOMETER_SECRET%:${CEILOMETER_SECRET}:g" -i ${WORKDIR}/ceilometer.conf

    sed -e "s:%DB_USER%:${DB_USER}:g" -i ${WORKDIR}/ceilometer.conf
    sed -e "s:%DB_PASSWORD%:${DB_PASSWORD}:g" -i ${WORKDIR}/ceilometer.conf

    sed -e "s:%CONTROLLER_IP%:${CONTROLLER_IP}:g" -i ${WORKDIR}/ceilometer.conf
    sed -e "s:%CONTROLLER_HOST%:${CONTROLLER_HOST}:g" -i ${WORKDIR}/ceilometer.conf

    sed -e "s:%COMPUTE_IP%:${COMPUTE_IP}:g" -i ${WORKDIR}/ceilometer.conf
    sed -e "s:%COMPUTE_HOST%:${COMPUTE_HOST}:g" -i ${WORKDIR}/ceilometer.conf

    sed -e "s:%ADMIN_PASSWORD%:${ADMIN_PASSWORD}:g" -i ${WORKDIR}/ceilometer.conf
    sed -e "s:%SERVICE_TENANT_NAME%:${SERVICE_TENANT_NAME}:g" -i ${WORKDIR}/ceilometer.conf

    install -d ${CEILOMETER_CONF_DIR}
    install -m 600 ${WORKDIR}/ceilometer.conf ${CEILOMETER_CONF_DIR}
    install -m 600 ${TEMPLATE_CONF_DIR}/*.json ${CEILOMETER_CONF_DIR}
    install -m 600 ${TEMPLATE_CONF_DIR}/*.yaml ${CEILOMETER_CONF_DIR}

    if ${@base_contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -d ${D}${sysconfdir}/init.d

        sed 's:@suffix@:api:' < ${WORKDIR}/ceilometer.init >${WORKDIR}/ceilometer-api.init.sh
        install -m 0755 ${WORKDIR}/ceilometer-api.init.sh ${D}${sysconfdir}/init.d/ceilometer-api

        sed 's:@suffix@:collector:' < ${WORKDIR}/ceilometer.init >${WORKDIR}/ceilometer-collector.init.sh
        install -m 0755 ${WORKDIR}/ceilometer-collector.init.sh ${D}${sysconfdir}/init.d/ceilometer-collector

        sed 's:@suffix@:agent-central:' < ${WORKDIR}/ceilometer.init >${WORKDIR}/ceilometer-agent-central.init.sh
        install -m 0755 ${WORKDIR}/ceilometer-agent-central.init.sh ${D}${sysconfdir}/init.d/ceilometer-agent-central

        sed 's:@suffix@:agent-compute:' < ${WORKDIR}/ceilometer.init >${WORKDIR}/ceilometer-agent-compute.init.sh
        install -m 0755 ${WORKDIR}/ceilometer-agent-compute.init.sh ${D}${sysconfdir}/init.d/ceilometer-agent-compute
    fi

    cp run-tests.sh ${CEILOMETER_CONF_DIR}
}

pkg_postinst_${SRCNAME}-setup () {
    if [ "x$D" != "x" ]; then
        exit 1
    fi

    # This is to make sure postgres is configured and running
    if ! pidof postmaster > /dev/null; then
       /etc/init.d/postgresql-init
       /etc/init.d/postgresql start
       sleep 2
    fi
    
    mkdir /var/log/ceilometer
    sudo -u postgres createdb ceilometer
    ceilometer-dbsync
}

inherit setuptools identity hosts update-rc.d

PACKAGES += " ${SRCNAME}-tests"
PACKAGES += "${SRCNAME}-setup ${SRCNAME}-common ${SRCNAME}-api ${SRCNAME}-collector ${SRCNAME}-compute ${SRCNAME}-controller"

ALLOW_EMPTY_${SRCNAME}-setup = "1"

FILES_${PN} = "${libdir}/*"

FILES_${SRCNAME}-tests = "${sysconfdir}/${SRCNAME}/run-tests.sh"

FILES_${SRCNAME}-common = "${sysconfdir}/${SRCNAME}/* \
"

FILES_${SRCNAME}-api = "${bindir}/ceilometer-api \
                        ${sysconfdir}/init.d/ceilometer-api \
"

FILES_${SRCNAME}-collector = "${bindir}/ceilometer-collector \
                              ${bindir}/ceilometer-collector-udp \
                              ${sysconfdir}/init.d/ceilometer-collector \
"
FILES_${SRCNAME}-compute = "${bindir}/ceilometer-agent-compute \
                            ${sysconfdir}/init.d/ceilometer-agent-compute \
"

FILES_${SRCNAME}-controller = "${bindir}/* \
                               ${localstatedir}/* \
                               ${sysconfdir}/init.d/ceilometer-agent-central \
"

RDEPENDS_${PN} += " \
        python-sqlalchemy \
	python-amqplib \
	python-anyjson \
	python-eventlet \
	python-kombu \
	python-lxml \
	python-routes \
	python-webob \
	python-greenlet \
	python-lockfile \
	python-pastedeploy \
	python-paste \
	python-sqlalchemy-migrate \
	python-stevedore \
	python-suds \
	python-paramiko \
	python-babel \
	python-iso8601 \
	python-setuptools-git \
	python-glanceclient \
	python-keystoneclient \
	python-swiftclient \
	python-oslo.config \
        python-msgpack \
        python-pecan \
        python-amqp \
        python-singledispatch \
        python-flask \
        python-werkzeug \
        python-itsdangerous \
        python-happybase \
        python-wsme \
        python-eventlet \
        python-pymongo \
        python-thrift \
        python-simplegeneric \
        python-webtest \
        python-waitress \
        python-pyyaml \
        python-pip \
        python-pytz \
	"

RDEPENDS_${SRCNAME}-controller = "${PN} ${SRCNAME}-common postgresql postgresql-client python-psycopg2 tgt"
RDEPENDS_${SRCNAME}-api = "${SRCNAME}-controller"
RDEPENDS_${SRCNAME}-collector = "${SRCNAME}-controller"
RDEPENDS_${SRCNAME}-compute = "${PN} ${SRCNAME}-common python-ceilometerclient libvirt"

INITSCRIPT_PACKAGES = "${SRCNAME}-api ${SRCNAME}-collector ${SRCNAME}-compute ${SRCNAME}-controller"
INITSCRIPT_NAME_${SRCNAME}-api = "${SRCNAME}-api"
INITSCRIPT_NAME_${SRCNAME}-collector = "${SRCNAME}-collector"
INITSCRIPT_NAME_${SRCNAME}-compute = "${SRCNAME}-agent-compute"
INITSCRIPT_NAME_${SRCNAME}-controller = "${SRCNAME}-agent-central"
