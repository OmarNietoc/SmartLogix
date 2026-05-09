import { PrismaClient as UsersClient } from './generated/users/index.js';
import { PrismaClient as InventoryClient } from './generated/inventory/index.js';

const users = new UsersClient();
const inventory = new InventoryClient();

const ids = {
  company: '11111111-1111-4111-8111-111111111111',
  roles: {
    ADMIN: '22222222-2222-4222-8222-222222222201',
    OPERATOR: '22222222-2222-4222-8222-222222222202',
    DRIVER: '22222222-2222-4222-8222-222222222203',
    VIEWER: '22222222-2222-4222-8222-222222222204',
  },
  users: {
    admin: '33333333-3333-4333-8333-333333333301',
    operator: '33333333-3333-4333-8333-333333333302',
    driver: '33333333-3333-4333-8333-333333333303',
  },
  products: {
    kit: '44444444-4444-4444-8444-444444444401',
    label: '44444444-4444-4444-8444-444444444402',
    box: '44444444-4444-4444-8444-444444444403',
  },
  warehouses: {
    main: '55555555-5555-4555-8555-555555555501',
    transit: '55555555-5555-4555-8555-555555555502',
  },
  stock: {
    kitMain: '66666666-6666-4666-8666-666666666601',
    labelMain: '66666666-6666-4666-8666-666666666602',
    boxTransit: '66666666-6666-4666-8666-666666666603',
  },
};

async function seedUsers() {
  await users.company.upsert({
    where: { id: ids.company },
    update: {
      taxId: '76.123.456-7',
      name: 'SmartLogix Demo',
      contactEmail: 'admin@smartlogix.cl',
      phone: '+56 2 2345 6789',
    },
    create: {
      id: ids.company,
      taxId: '76.123.456-7',
      name: 'SmartLogix Demo',
      contactEmail: 'admin@smartlogix.cl',
      phone: '+56 2 2345 6789',
    },
  });

  for (const [name, id] of Object.entries(ids.roles)) {
    await users.role.upsert({
      where: { name },
      update: { id },
      create: { id, name },
    });
  }

  const profiles = [
    { id: ids.users.admin, authId: 'admin@smartlogix.cl', firstName: 'Admin', lastName: 'SmartLogix', role: 'ADMIN' },
    { id: ids.users.operator, authId: 'operador@smartlogix.cl', firstName: 'Operador', lastName: 'SmartLogix', role: 'OPERATOR' },
    { id: ids.users.driver, authId: 'conductor@smartlogix.cl', firstName: 'Conductor', lastName: 'SmartLogix', role: 'DRIVER' },
  ];

  for (const profile of profiles) {
    await users.userProfile.upsert({
      where: { authId: profile.authId },
      update: {
        firstName: profile.firstName,
        lastName: profile.lastName,
        companyId: ids.company,
      },
      create: {
        id: profile.id,
        authId: profile.authId,
        companyId: ids.company,
        firstName: profile.firstName,
        lastName: profile.lastName,
      },
    });

    await users.userRole.upsert({
      where: {
        userId_roleId: {
          userId: profile.id,
          roleId: ids.roles[profile.role],
        },
      },
      update: {},
      create: {
        userId: profile.id,
        roleId: ids.roles[profile.role],
      },
    });
  }
}

async function seedInventory() {
  const products = [
    { id: ids.products.kit, sku: 'PROD-001', name: 'Kit logístico inicial', price: '12990.00' },
    { id: ids.products.label, sku: 'LBL-THERM-100', name: 'Etiquetas térmicas pack 100', price: '6990.00' },
    { id: ids.products.box, sku: 'BOX-M-25', name: 'Cajas medianas pack 25', price: '15990.00' },
  ];

  for (const product of products) {
    await inventory.product.upsert({
      where: { id: product.id },
      update: {
        companyId: ids.company,
        sku: product.sku,
        name: product.name,
        price: product.price,
        status: 'ACTIVE',
      },
      create: {
        id: product.id,
        companyId: ids.company,
        sku: product.sku,
        name: product.name,
        price: product.price,
        status: 'ACTIVE',
      },
    });
  }

  const warehouses = [
    { id: ids.warehouses.main, name: 'Bodega Central Santiago', locationAddress: 'Av. Las Condes 1234, Las Condes, Región Metropolitana', type: 'WAREHOUSE' },
    { id: ids.warehouses.transit, name: 'Hub Última Milla Maipú', locationAddress: 'Camino Melipilla 9100, Maipú, Región Metropolitana', type: 'RETAIL_STORE' },
  ];

  for (const warehouse of warehouses) {
    await inventory.warehouse.upsert({
      where: { id: warehouse.id },
      update: {
        companyId: ids.company,
        name: warehouse.name,
        locationAddress: warehouse.locationAddress,
        type: warehouse.type,
        status: 'ACTIVE',
      },
      create: {
        id: warehouse.id,
        companyId: ids.company,
        name: warehouse.name,
        locationAddress: warehouse.locationAddress,
        type: warehouse.type,
        status: 'ACTIVE',
      },
    });
  }

  const stock = [
    { id: ids.stock.kitMain, productId: ids.products.kit, warehouseId: ids.warehouses.main, stockAvailable: 40 },
    { id: ids.stock.labelMain, productId: ids.products.label, warehouseId: ids.warehouses.main, stockAvailable: 120 },
    { id: ids.stock.boxTransit, productId: ids.products.box, warehouseId: ids.warehouses.transit, stockAvailable: 65 },
  ];

  for (const entry of stock) {
    await inventory.inventory.upsert({
      where: {
        productId_warehouseId: {
          productId: entry.productId,
          warehouseId: entry.warehouseId,
        },
      },
      update: {
        stockAvailable: entry.stockAvailable,
        stockReserved: 0,
        lastUpdated: new Date(),
      },
      create: {
        id: entry.id,
        productId: entry.productId,
        warehouseId: entry.warehouseId,
        stockAvailable: entry.stockAvailable,
        stockReserved: 0,
        lastUpdated: new Date(),
      },
    });
  }
}

try {
  await seedUsers();
  await seedInventory();
  console.log('SmartLogix seed completado: usuarios, productos, bodegas y stock creados.');
} finally {
  await users.$disconnect();
  await inventory.$disconnect();
}
