const eddsa = require('./eddsa.js');

caseTemplate = 'new JsCrossCheckCase(\n'+
               '"$msg",\n'+
               '"$rx",\n'+
               '"$ry",\n'+
               '"$ss",\n'+
               '"$pkx",\n'+
               '"$pky"\n'+
               '),'

for (i = 0; i < 100; i++) {
    nk = eddsa.getKeyPair();
    sign = eddsa.sign(nk.secretKey, nk.secretKey);

    c = caseTemplate.replace('$msg', nk.secretKey)
	.replace('$rx', sign.Rx)
	.replace('$ry', sign.Ry)
	.replace('$ss', sign.s)
	.replace('$pkx', nk.publicKeyX)
    	.replace('$pky', nk.publicKeyY)
    console.log(c);
}
