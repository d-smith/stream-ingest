# formatter

sls deploy --region us-west-2 

sls remove --region us-west-2
(base) ds@lappy xform % sls invoke -f formatter --region us-west-2 --stage dev --data 'this is a test'


(base) ds@lappy xform % sls logs --region us-west-2 --function formatter                              
