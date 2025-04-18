import Model from 'can-connect/can/model/model';

export default Model.extend({
  findAll: 'GET api/v2/admin/server/templates',
  findOne: 'GET api/v2/admin/server/templates/{key}',
  destroy: 'POST api/v2/admin/server/templates/delete',
  create: 'POST api/v2/admin/server/templates/{key}',
  update: 'POST api/v2/admin/server/templates/{key}'
}, {});
